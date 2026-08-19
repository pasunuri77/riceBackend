package com.rice.controller;

import com.rice.dto.customer.CreateCustomerRequest;
import com.rice.dto.customer.CustomerResponse;
import com.rice.dto.customer.UpdateStatusRequest;
import com.rice.security.AppUserPrincipal;
import com.rice.service.CustomerService;
import com.rice.service.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/customers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or @permCheck.canManageCustomers(authentication)")
public class CustomerController {

    private final CustomerService customerService;
    private final StaffService staffService;

    @GetMapping
    public List<CustomerResponse> list() {
        return customerService.list();
    }

    @GetMapping("/{id}")
    public CustomerResponse getById(@PathVariable Long id) {
        return customerService.getById(id);
    }

    @PostMapping
    public CustomerResponse create(@Valid @RequestBody CreateCustomerRequest request) {
        return customerService.create(request);
    }

    @PatchMapping("/{id}/status")
    public CustomerResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request) {
        return customerService.updateStatus(id, request.getStatus());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, @AuthenticationPrincipal AppUserPrincipal principal) {
        staffService.delete(id, principal.getUser());
    }
}
