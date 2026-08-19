package com.rice.controller;

import com.rice.dto.returnrequest.ReturnRequestResponseDto;
import com.rice.dto.returnrequest.ReturnRequestSubmissionDto;
import com.rice.dto.returnrequest.ReturnableItemsResponseDto;
import com.rice.entity.User;
import com.rice.service.ReturnRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ReturnRequestController {

    private final ReturnRequestService returnRequestService;

    public ReturnRequestController(ReturnRequestService returnRequestService) {
        this.returnRequestService = returnRequestService;
    }

    @GetMapping("/orders/{orderId}/returnable-items")
    public ResponseEntity<ReturnableItemsResponseDto> getReturnableItems(
            @PathVariable Long orderId,
            @AuthenticationPrincipal User user) {
        ReturnableItemsResponseDto response = returnRequestService.getReturnableItems(orderId, user.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/return-requests")
    public ResponseEntity<?> submitReturnRequest(
            @Valid @RequestBody ReturnRequestSubmissionDto dto,
            @AuthenticationPrincipal User user) {
        try {
            ReturnRequestResponseDto response = returnRequestService.submitReturnRequest(dto, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException ex) {
            if (ex.getMessage().contains("quantity exceeds") || ex.getMessage().contains("expired")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
            }
            if (ex.getMessage().contains("Forbidden")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
            }
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/me/return-requests")
    public ResponseEntity<List<ReturnRequestResponseDto>> getMyReturnRequests(
            @AuthenticationPrincipal User user) {
        List<ReturnRequestResponseDto> response = returnRequestService.getCustomerReturnRequests(user.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/return-requests/{id}")
    public ResponseEntity<ReturnRequestResponseDto> getReturnRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        try {
            ReturnRequestResponseDto response = returnRequestService.getReturnRequestByIdForCustomer(id, user.getId());
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
