package com.rice.controller;

import com.rice.dto.address.AddressRequest;
import com.rice.dto.address.AddressResponse;
import com.rice.security.AppUserPrincipal;
import com.rice.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public List<AddressResponse> list(@AuthenticationPrincipal AppUserPrincipal principal) {
        return addressService.list(principal.getUser());
    }

    @PostMapping
    public AddressResponse create(@AuthenticationPrincipal AppUserPrincipal principal, @RequestBody AddressRequest request) {
        return addressService.create(principal.getUser(), request);
    }

    @PutMapping("/{id}")
    public AddressResponse update(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable Long id, @RequestBody AddressRequest request) {
        return addressService.update(principal.getUser(), id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable Long id) {
        addressService.delete(principal.getUser(), id);
    }

    @PatchMapping("/{id}/default")
    public List<AddressResponse> setDefault(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable Long id) {
        return addressService.setDefault(principal.getUser(), id);
    }
}
