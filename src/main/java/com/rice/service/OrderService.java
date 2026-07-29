package com.rice.service;

import com.rice.dto.order.OrderCreateRequest;
import com.rice.dto.order.OrderItemRequest;
import com.rice.dto.order.OrderResponse;
import com.rice.entity.Order;
import com.rice.entity.OrderItem;
import com.rice.entity.Product;
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
    private static final BigDecimal FREE_DELIVERY_THRESHOLD = BigDecimal.valueOf(999);
    private static final BigDecimal DELIVERY_CHARGE = BigDecimal.valueOf(49);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    public List<OrderResponse> listAll() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    public List<OrderResponse> listByCustomer(Long customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream().map(this::toResponse).toList();
    }

    public OrderResponse getById(Long id) {
        return toResponse(find(id));
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
        order.setDeliveryStatus(parseDeliveryStatusValue(status));
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
        BigDecimal deliveryCharge = subtotal.compareTo(FREE_DELIVERY_THRESHOLD) > 0 || subtotal.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO : DELIVERY_CHARGE;
        BigDecimal total = subtotal.add(deliveryCharge);

        PaymentMethod method = parsePaymentMethod(req.getPaymentMethod());

        Order order = Order.builder()
                .customer(customer)
                .addressSnapshot(req.getAddress())
                .paymentMethod(method)
                .paymentStatus(method == PaymentMethod.COD ? PaymentStatus.PENDING : PaymentStatus.PAID)
                .amount(total)
                .build();

        for (OrderItemRequest itemReq : req.getItems()) {
            Product product = itemReq.getId() == null ? null : productRepository.findById(itemReq.getId()).orElse(null);
            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .productNameSnapshot(itemReq.getName())
                    .imageSnapshot(itemReq.getImage())
                    .weightKg(itemReq.getWeight())
                    .qty(itemReq.getQty())
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
