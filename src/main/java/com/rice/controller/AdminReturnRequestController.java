package com.rice.controller;

import com.rice.dto.returnrequest.ApproveReturnDto;
import com.rice.dto.returnrequest.ProcessRefundDto;
import com.rice.dto.returnrequest.ReceiveReturnDto;
import com.rice.dto.returnrequest.RejectReturnDto;
import com.rice.dto.returnrequest.ReturnRequestResponseDto;
import com.rice.service.ReturnRequestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/return-requests")
@PreAuthorize("hasRole('ADMIN') or @permCheck.canManageOrders(authentication)")
public class AdminReturnRequestController {

    private final ReturnRequestService returnRequestService;

    public AdminReturnRequestController(ReturnRequestService returnRequestService) {
        this.returnRequestService = returnRequestService;
    }

    @GetMapping
    public ResponseEntity<List<ReturnRequestResponseDto>> getAllReturnRequests(
            @RequestParam(required = false, defaultValue = "all") String status) {
        List<ReturnRequestResponseDto> response = returnRequestService.getAllReturnRequests(status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReturnRequestResponseDto> getReturnRequestDetails(@PathVariable Long id) {
        try {
            ReturnRequestResponseDto response = returnRequestService.getReturnRequestByIdForAdmin(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveReturnRequest(
            @PathVariable Long id,
            @Valid @RequestBody ApproveReturnDto dto) {
        try {
            ReturnRequestResponseDto response = returnRequestService.approveReturnRequest(id, dto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectReturnRequest(
            @PathVariable Long id,
            @Valid @RequestBody RejectReturnDto dto) {
        try {
            ReturnRequestResponseDto response = returnRequestService.rejectReturnRequest(id, dto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/{id}/receive")
    public ResponseEntity<?> receiveReturnItems(
            @PathVariable Long id,
            @Valid @RequestBody ReceiveReturnDto dto) {
        try {
            ReturnRequestResponseDto response = returnRequestService.receiveReturnItems(id, dto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<?> processRefund(
            @PathVariable Long id,
            @Valid @RequestBody ProcessRefundDto dto) {
        try {
            ReturnRequestResponseDto response = returnRequestService.processRefund(id, dto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
