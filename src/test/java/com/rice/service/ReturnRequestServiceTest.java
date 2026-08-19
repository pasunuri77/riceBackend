package com.rice.service;

import com.rice.dto.returnrequest.ReturnItemRequestDto;
import com.rice.dto.returnrequest.ReturnRequestResponseDto;
import com.rice.dto.returnrequest.ReturnRequestSubmissionDto;
import com.rice.dto.returnrequest.ApproveReturnDto;
import com.rice.entity.*;
import com.rice.entity.enums.RefundMethod;
import com.rice.entity.enums.ReturnRequestStatus;
import com.rice.repository.OrderRepository;
import com.rice.repository.RefundRepository;
import com.rice.repository.ReturnRequestItemRepository;
import com.rice.repository.ReturnRequestRepository;
import com.rice.service.impl.ReturnRequestServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReturnRequestServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ReturnRequestRepository returnRequestRepository;
    @Mock
    private ReturnRequestItemRepository returnRequestItemRepository;
    @Mock
    private RefundRepository refundRepository;

    @InjectMocks
    private ReturnRequestServiceImpl returnRequestService;

    private User customer;
    private Order order;
    private OrderItem item10lb;
    private OrderItem item20lb;

    @BeforeEach
    void setUp() {
        customer = new User();
        customer.setId(1L);

        order = new Order();
        order.setId(100L);
        order.setCustomer(customer);
        order.setDeliveredAt(Instant.now().minusSeconds(86400)); // Delivered yesterday

        Product product1 = new Product();
        product1.setId("p1");

        item10lb = new OrderItem();
        item10lb.setId(10L);
        item10lb.setOrder(order);
        item10lb.setProduct(product1);
        item10lb.setProductNameSnapshot("Rice");
        item10lb.setWeightKg(10); // Not price, it's weight
        item10lb.setQty(2);
        item10lb.setPricePerKgSnapshot(new BigDecimal("0.3")); // 0.3 * 10 = $3.00 unit price

        item20lb = new OrderItem();
        item20lb.setId(20L);
        item20lb.setOrder(order);
        item20lb.setProduct(product1);
        item20lb.setProductNameSnapshot("Rice");
        item20lb.setWeightKg(20);
        item20lb.setQty(2);
        item20lb.setPricePerKgSnapshot(new BigDecimal("0.3")); // 0.3 * 20 = $6.00 unit price

        order.setItems(List.of(item10lb, item20lb));
    }

    @Test
    void submitReturnRequest_Success_PartialQuantity() {
        ReturnRequestSubmissionDto dto = new ReturnRequestSubmissionDto();
        dto.setOrderId(100L);
        dto.setReason("QUALITY_ISSUE");
        dto.setRefundMethod(RefundMethod.ORIGINAL_PAYMENT_METHOD);

        ReturnItemRequestDto reqItem1 = new ReturnItemRequestDto();
        reqItem1.setOrderItemId(10L);
        reqItem1.setQuantity(1);

        ReturnItemRequestDto reqItem2 = new ReturnItemRequestDto();
        reqItem2.setOrderItemId(20L);
        reqItem2.setQuantity(1);

        dto.setItems(List.of(reqItem1, reqItem2));

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(returnRequestItemRepository.findActiveReturnsByOrderItemId(any())).thenReturn(List.of());
        
        when(returnRequestRepository.save(any(ReturnRequest.class))).thenAnswer(invocation -> {
            ReturnRequest req = invocation.getArgument(0);
            req.setId(500L);
            return req;
        });

        ReturnRequestResponseDto response = returnRequestService.submitReturnRequest(dto, customer);

        assertNotNull(response);
        assertEquals(new BigDecimal("9.00"), response.getRefundAmount()); // 3 + 6
        assertEquals(ReturnRequestStatus.PENDING, response.getStatus());
        assertEquals(2, response.getItems().size());
    }

    @Test
    void submitReturnRequest_Fails_WhenQuantityExceeds() {
        ReturnRequestSubmissionDto dto = new ReturnRequestSubmissionDto();
        dto.setOrderId(100L);
        dto.setReason("QUALITY_ISSUE");
        dto.setRefundMethod(RefundMethod.ORIGINAL_PAYMENT_METHOD);

        ReturnItemRequestDto reqItem1 = new ReturnItemRequestDto();
        reqItem1.setOrderItemId(10L);
        reqItem1.setQuantity(3); // ordered qty is 2

        dto.setItems(List.of(reqItem1));

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(returnRequestItemRepository.findActiveReturnsByOrderItemId(any())).thenReturn(List.of());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            returnRequestService.submitReturnRequest(dto, customer);
        });
        
        assertTrue(exception.getMessage().contains("quantity exceeds"));
    }

    @Test
    void approveReturnRequest_Success() {
        ReturnRequest request = new ReturnRequest();
        request.setId(500L);
        request.setOrder(order);
        request.setStatus(ReturnRequestStatus.PENDING);
        request.setRefundMethod(RefundMethod.ORIGINAL_PAYMENT_METHOD);
        
        ReturnRequestItem rri = new ReturnRequestItem();
        rri.setId(1L);
        rri.setOrderItem(item10lb);
        rri.setRequestedQuantity(1);
        rri.setUnitPrice(new BigDecimal("3.00"));
        request.setItems(List.of(rri));

        when(returnRequestRepository.findById(500L)).thenReturn(Optional.of(request));
        when(returnRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ApproveReturnDto dto = new ApproveReturnDto();
        dto.setAdminNote("Approved");

        ReturnRequestResponseDto response = returnRequestService.approveReturnRequest(500L, dto);

        assertEquals(ReturnRequestStatus.APPROVED, response.getStatus());
        assertEquals(new BigDecimal("3.00"), response.getRefundAmount());
        assertEquals("Approved", response.getAdminNote());
        
        verify(refundRepository, times(1)).save(any(Refund.class));
    }
}
