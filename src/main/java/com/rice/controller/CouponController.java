package com.rice.controller;

import com.rice.dto.coupon.CouponRequest;
import com.rice.dto.coupon.CouponResponse;
import com.rice.dto.coupon.CouponValidationResponse;
import com.rice.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @GetMapping("/coupons")
    public List<CouponResponse> list() {
        return couponService.listAll();
    }

    @PostMapping("/admin/coupons")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN') or @permCheck.canManageCoupons(authentication)")
    public CouponResponse create(@Valid @RequestBody CouponRequest request) {
        return couponService.create(request);
    }

    @PutMapping("/admin/coupons/{id}")
    @PreAuthorize("hasRole('ADMIN') or @permCheck.canManageCoupons(authentication)")
    public CouponResponse update(@PathVariable Long id, @Valid @RequestBody CouponRequest request) {
        return couponService.update(id, request);
    }

    @DeleteMapping("/admin/coupons/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN') or @permCheck.canManageCoupons(authentication)")
    public void delete(@PathVariable Long id) {
        couponService.delete(id);
    }

    @PostMapping("/coupons/validate")
    public CouponValidationResponse validate(@Valid @RequestBody com.rice.dto.coupon.CouponValidateRequest request) {
        return couponService.validateCoupon(request.getCode(), request.getSubtotal());
    }
}
