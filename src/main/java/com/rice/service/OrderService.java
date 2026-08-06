package com.rice.service;

import com.rice.dto.order.OrderCreateRequest;
import com.rice.dto.order.OrderItemRequest;
import com.rice.dto.order.OrderResponse;
import com.rice.entity.Order;
import com.rice.entity.OrderItem;
import com.rice.entity.Product;
import com.rice.entity.StoreSettings;
import com.rice.entity.User;
import com.rice.entity.enums.PaymentMethod;
import com.rice.entity.enums.PaymentStatus;
import com.rice.entity.enums.DeliveryStatus;
import com.rice.exception.ApiException;
import com.rice.repository.OrderRepository;
import com.rice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final StoreSettingsService storeSettingsService;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    public List<OrderResponse> listAll() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    public List<OrderResponse> listByCustomer(Long customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream().map(this::toResponse).toList();
    }

    public OrderResponse getById(Long id, User currentUser) {
        Order order = find(id);
        if (!isAdmin(currentUser) && !order.getCustomer().getId().equals(currentUser.getId())) {
            throw ApiException.notFound("Order not found: " + id);
        }
        return toResponse(order);
    }

    @Transactional
    public OrderResponse updatePaymentStatus(String displayId, String status) {
        Order order = find(parseDisplayId(displayId));
        order.setPaymentStatus(parsePaymentStatusValue(status));
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse updateDeliveryStatus(String displayId, String status) {
        Order order = find(parseDisplayId(displayId));
        DeliveryStatus previous = order.getDeliveryStatus();
        DeliveryStatus next = parseDeliveryStatusValue(status);

        if (previous != DeliveryStatus.CANCELLED && next == DeliveryStatus.CANCELLED) {
            restoreStock(order);
        } else if (previous == DeliveryStatus.CANCELLED && next != DeliveryStatus.CANCELLED) {
            decrementStock(order);
        }

        order.setDeliveryStatus(next);
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse cancel(String displayId, User customer) {
        Order order = find(parseDisplayId(displayId));
        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw ApiException.forbidden("Order not found: " + displayId);
        }
        if (order.getDeliveryStatus() != DeliveryStatus.PENDING
                && order.getDeliveryStatus() != DeliveryStatus.PROCESSING) {
            throw ApiException.badRequest("Order can no longer be cancelled");
        }

        restoreStock(order);
        order.setDeliveryStatus(DeliveryStatus.CANCELLED);
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse create(User customer, OrderCreateRequest req) {
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw ApiException.badRequest("Cannot place an order with no items");
        }

        BigDecimal subtotal = req.getItems().stream()
                .map(this::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal deliveryCharge = deliveryChargeFor(subtotal);
        StoreSettings settings = storeSettingsService.current();
        BigDecimal tax = subtotal.multiply(defaultMoney(settings.getTaxPercentage()))
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(deliveryCharge).add(tax);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        PaymentMethod method = parsePaymentMethod(req.getPaymentMethod());

        Order order = Order.builder()
                .customer(customer)
                .addressSnapshot(req.getAddress())
                .notes(req.getNotes())
                .paymentMethod(method)
                .paymentStatus(method == PaymentMethod.COD ? PaymentStatus.PENDING : PaymentStatus.PAID)
                .amount(total)
                .build();

        for (OrderItemRequest itemReq : req.getItems()) {
            if (itemReq.getId() == null) {
                throw ApiException.badRequest("Order item is missing product id");
            }
            Product product = productRepository.findByIdForUpdate(itemReq.getId()).orElseThrow(
                    () -> ApiException.badRequest("Product not found: " + itemReq.getId()));
            int qty = itemReq.getQty() == null ? 0 : itemReq.getQty();
            if (qty <= 0) {
                throw ApiException.badRequest("Order item quantity must be greater than zero");
            }
            if (product.getStock() == null || product.getStock() < qty) {
                throw ApiException.badRequest("Insufficient stock for product: " + product.getId());
            }
            product.setStock(product.getStock() - qty);

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .productNameSnapshot(itemReq.getName())
                    .imageSnapshot(itemReq.getImage())
                    .weightKg(itemReq.getWeight())
                    .qty(qty)
                    .pricePerKgSnapshot(itemReq.getPricePerKg())
                    .build();
            order.getItems().add(item);
        }

        return toResponse(orderRepository.save(order));
    }

    private BigDecimal lineTotal(OrderItemRequest item) {
        return item.getPricePerKg()
                .multiply(BigDecimal.valueOf(item.getWeight()))
                .multiply(BigDecimal.valueOf(item.getQty()));
    }

    private BigDecimal deliveryChargeFor(BigDecimal subtotal) {
        if (subtotal.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        StoreSettings settings = storeSettingsService.current();
        BigDecimal threshold = nonNegative(settings.getFreeDeliveryThreshold());
        if (threshold.compareTo(BigDecimal.ZERO) > 0 && subtotal.compareTo(threshold) >= 0) {
            return BigDecimal.ZERO;
        }
        return nonNegative(settings.getDeliveryCharge());
    }

    private BigDecimal nonNegative(BigDecimal value) {
        return value == null || value.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : value;
    }

    private BigDecimal defaultMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private boolean isAdmin(User user) {
        return user != null && user.getRole() != null && user.getRole().name().equalsIgnoreCase("ADMIN");
    }

    private void decrementStock(Order order) {
        for (OrderItem item : order.getItems()) {
            if (item.getProduct() == null) {
                continue;
            }
            String productId = item.getProduct().getId();
            Product product = productRepository.findByIdForUpdate(productId)
                    .orElseThrow(() -> ApiException.badRequest("Product not found: " + productId));
            int qty = item.getQty() == null ? 0 : item.getQty();
            if (qty <= 0) {
                continue;
            }
            if (product.getStock() == null || product.getStock() < qty) {
                throw ApiException.badRequest("Insufficient stock to restore order: " + product.getId());
            }
            product.setStock(product.getStock() - qty);
        }
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            if (item.getProduct() == null) {
                continue;
            }
            String productId = item.getProduct().getId();
            Product product = productRepository.findByIdForUpdate(productId)
                    .orElseThrow(() -> ApiException.badRequest("Product not found: " + productId));
            product.setStock((product.getStock() == null ? 0 : product.getStock()) + item.getQty());
        }
    }

    private PaymentMethod parsePaymentMethod(String value) {
        if (value == null) return PaymentMethod.COD;
        for (PaymentMethod m : PaymentMethod.values()) {
            if (m.name().equalsIgnoreCase(value)) return m;
        }
        return PaymentMethod.COD;
    }

    private PaymentStatus parsePaymentStatusValue(String value) {
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.name().equals(normalizeStatus(value))) return status;
        }
        throw ApiException.badRequest("Invalid payment status: " + value);
    }

    private DeliveryStatus parseDeliveryStatusValue(String value) {
        for (DeliveryStatus status : DeliveryStatus.values()) {
            if (status.name().equals(normalizeStatus(value))) return status;
        }
        throw ApiException.badRequest("Invalid delivery status: " + value);
    }

    private String normalizeStatus(String value) {
        return value == null ? "" : value.trim().replace('-', '_').replace(' ', '_').toUpperCase(Locale.ROOT);
    }

    private Long parseDisplayId(String displayId) {
        if (displayId == null || !displayId.toUpperCase(Locale.ROOT).startsWith("ORD")) {
            throw ApiException.badRequest("Invalid order id: " + displayId);
        }
        try {
            long displayNumber = Long.parseLong(displayId.substring(3));
            long dbId = displayNumber - 10000;
            if (dbId <= 0) {
                throw ApiException.badRequest("Invalid order id: " + displayId);
            }
            return dbId;
        } catch (NumberFormatException ex) {
            throw ApiException.badRequest("Invalid order id: " + displayId);
        }
    }

    private Order find(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> ApiException.notFound("Order not found: " + id));
    }

    private OrderResponse toResponse(Order o) {
        List<OrderItem> items = o.getItems();
        String riceName = items.isEmpty() ? "" :
                items.size() == 1 ? items.get(0).getProductNameSnapshot()
                        : items.get(0).getProductNameSnapshot() + " +" + (items.size() - 1) + " more";
        String quantity = items.stream()
                .map(i -> i.getWeightKg() + "kg x" + i.getQty())
                .collect(Collectors.joining(", "));
        String productId = items.isEmpty() || items.get(0).getProduct() == null ? null : items.get(0).getProduct().getId();
        String image = items.isEmpty() ? null : items.get(0).getImageSnapshot();
        List<OrderResponse.ItemResponse> itemResponses = items.stream()
                .map(i -> OrderResponse.ItemResponse.builder()
                        .name(i.getProductNameSnapshot())
                        .weight(i.getWeightKg())
                        .qty(i.getQty())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id("ORD" + (10000 + o.getId()))
                .customerId(o.getCustomer().getId().toString())
                .customerName(o.getCustomer().getName())
                .productId(productId)
                .riceName(riceName)
                .image(image)
                .address(o.getAddressSnapshot())
                .quantity(quantity)
                .amount(o.getAmount())
                .paymentStatus(capitalize(o.getPaymentStatus().name()))
                .deliveryStatus(capitalize(o.getDeliveryStatus().name()))
                .date(DATE_FORMAT.format(o.getCreatedAt().atZone(ZoneOffset.UTC)))
                .items(itemResponses)
                .build();
    }

    private String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
