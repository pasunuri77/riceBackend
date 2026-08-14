package com.rice.service;

import com.rice.dto.order.OrderCreateRequest;
import com.rice.dto.order.OrderItemRequest;
import com.rice.dto.order.OrderResponse;
import com.rice.email.EmailService;
import com.rice.entity.Order;
import com.rice.entity.OrderItem;
import com.rice.entity.Product;
import com.rice.entity.StoreSettings;
import com.rice.entity.User;
import com.rice.entity.enums.PaymentMethod;
import com.rice.entity.enums.PaymentStatus;
import com.rice.entity.enums.DeliveryStatus;
import com.rice.service.CouponService;
import com.rice.exception.ApiException;
import com.rice.repository.OrderRepository;
import com.rice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
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
    private final CouponService couponService;
    private final EmailService emailService;
    private final com.rice.service.ProductAnalyticsService productAnalyticsService;
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
        Order saved = orderRepository.save(order);
        // log purchase events for analytics (one event per order item)
        try {
            for (OrderItem item : saved.getItems()) {
                if (item.getProduct() != null) {
                    productAnalyticsService.logEvent(item.getProduct().getId(), com.rice.entity.enums.ProductEventType.PURCHASE);
                }
            }
        } catch (Exception ignored) {
        }
        return toResponse(saved);
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

        if (previous != DeliveryStatus.DELIVERED && next == DeliveryStatus.DELIVERED) {
            Instant deliveredAt = Instant.now();
            order.setDeliveredAt(deliveredAt);
            order.setDelayFlag(order.getEstimatedDeliveryDate() != null && deliveredAt.isAfter(order.getEstimatedDeliveryDate()));
        }

        order.setDeliveryStatus(next);
        Order saved = orderRepository.save(order);

        if (saved.getCustomer() != null && saved.getCustomer().getEmail() != null) {
            String orderId = "ORD" + (10000 + saved.getId());
            if (previous != DeliveryStatus.PROCESSING && next == DeliveryStatus.PROCESSING) {
                emailService.sendOrderAcceptedEmail(saved.getCustomer().getEmail(), saved.getCustomer().getName(), orderId);
            } else if (previous != DeliveryStatus.SHIPPED && next == DeliveryStatus.SHIPPED) {
                emailService.sendOrderShippedEmail(saved.getCustomer().getEmail(), saved.getCustomer().getName(), orderId);
            } else if (previous != DeliveryStatus.DELIVERED && next == DeliveryStatus.DELIVERED) {
                emailService.sendOrderDeliveredEmail(saved.getCustomer().getEmail(), saved.getCustomer().getName(), orderId);
            } else if (previous != DeliveryStatus.CANCELLED && next == DeliveryStatus.CANCELLED) {
                emailService.sendOrderCancelledEmail(saved.getCustomer().getEmail(), saved.getCustomer().getName(), orderId);
            }
        }

        return toResponse(saved);
    }

    @Transactional
    public OrderResponse updateDeliveryInfo(String displayId, com.rice.dto.order.UpdateOrderDeliveryInfoRequest request) {
        Order order = find(parseDisplayId(displayId));
        order.setEstimatedDeliveryDate(request.getEstimatedDeliveryDate());
        order.setDeliveryRemarks(request.getDeliveryRemarks());
        if (order.getDeliveryStatus() == DeliveryStatus.DELIVERED && order.getDeliveredAt() != null && order.getEstimatedDeliveryDate() != null) {
            order.setDelayFlag(order.getDeliveredAt().isAfter(order.getEstimatedDeliveryDate()));
        }
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

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (req.getCouponCode() != null && !req.getCouponCode().isBlank()) {
            var validation = couponService.validateCoupon(req.getCouponCode(), subtotal);
            if (!validation.isValid()) {
                throw ApiException.badRequest(validation.getMessage());
            }
            discountAmount = validation.getDiscountAmount();
        }

        BigDecimal total = subtotal.subtract(discountAmount).add(deliveryCharge).add(tax);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        PaymentMethod method = parsePaymentMethod(req.getPaymentMethod());

        Order order = Order.builder()
                .customer(customer)
                .addressSnapshot(req.getAddress())
                .notes(req.getNotes())
                .couponCode(req.getCouponCode() == null ? null : req.getCouponCode().trim().toUpperCase())
                .discountAmount(discountAmount)
                .subtotal(subtotal)
                .tax(tax)
                .deliveryCharge(deliveryCharge)
                .offerDiscount(BigDecimal.ZERO)
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
            int weight = itemReq.getWeight() == null ? 0 : itemReq.getWeight();
            if (qty <= 0) {
                throw ApiException.badRequest("Order item quantity must be greater than zero");
            }
            if (weight <= 0) {
                throw ApiException.badRequest("Order item weight must be specified");
            }
            int totalKg = weight * qty;
            if (product.getStock() == null || product.getStock() < totalKg) {
                throw ApiException.badRequest("Insufficient stock for product: " + product.getId());
            }
            decrementBagStock(product, weight, qty);

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
            int weightKg = item.getWeightKg() == null ? 0 : item.getWeightKg();
            if (qty <= 0 || weightKg <= 0) {
                continue;
            }
            int totalKg = weightKg * qty;
            decrementBagStock(product, weightKg, qty);
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
            int qty = item.getQty() == null ? 0 : item.getQty();
            int weightKg = item.getWeightKg() == null ? 0 : item.getWeightKg();
            if (qty <= 0 || weightKg <= 0) {
                continue;
            }
            product.setStock((product.getStock() == null ? 0 : product.getStock()) + (weightKg * qty));
            restoreBagStock(product, weightKg, qty);
        }
    }

    private void decrementBagStock(Product product, int weightKg, int qty) {
        int currentStock = product.getStock() == null ? 0 : product.getStock();
        int totalKg = weightKg * qty;
        if (currentStock < totalKg) {
            throw ApiException.badRequest("Insufficient stock for product: " + product.getId());
        }
        product.setStock(currentStock - totalKg);
        Integer currentBagStock = getBagStock(product, weightKg);
        if (currentBagStock != null) {
            if (currentBagStock < qty) {
                throw ApiException.badRequest("Insufficient " + weightKg + "kg bag stock for product: " + product.getId());
            }
            setBagStock(product, weightKg, currentBagStock - qty);
        }
    }

    private void restoreBagStock(Product product, int weightKg, int qty) {
        Integer currentBagStock = getBagStock(product, weightKg);
        if (currentBagStock != null) {
            setBagStock(product, weightKg, currentBagStock + qty);
        }
    }

    private Integer getBagStock(Product product, int weightKg) {
        return switch (weightKg) {
            case 1 -> product.getStock1Kg();
            case 5 -> product.getStock5Kg();
            case 10 -> product.getStock10Kg();
            case 50 -> product.getStock50Kg();
            default -> null;
        };
    }

    private void setBagStock(Product product, int weightKg, Integer value) {
        switch (weightKg) {
            case 1 -> product.setStock1Kg(value);
            case 5 -> product.setStock5Kg(value);
            case 10 -> product.setStock10Kg(value);
            case 50 -> product.setStock50Kg(value);
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
                .pricePerKg(i.getPricePerKgSnapshot())
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
                .notes(o.getNotes())
                .couponCode(o.getCouponCode())
                .discountAmount(o.getDiscountAmount())
                .subtotal(o.getSubtotal())
                .tax(o.getTax())
                .deliveryCharge(o.getDeliveryCharge())
                .offerDiscount(o.getOfferDiscount())
                .estimatedDeliveryDate(o.getEstimatedDeliveryDate())
                .deliveredAt(o.getDeliveredAt())
                .delayFlag(o.isDelayFlag())
                .deliveryRemarks(o.getDeliveryRemarks())
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
