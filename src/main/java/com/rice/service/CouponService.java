package com.rice.service;

import com.rice.dto.coupon.CouponRequest;
import com.rice.dto.coupon.CouponResponse;
import com.rice.dto.coupon.CouponValidationResponse;
import com.rice.entity.Coupon;
import com.rice.entity.enums.CouponType;
import com.rice.exception.ApiException;
import com.rice.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

    private final CouponRepository couponRepository;

    public List<CouponResponse> listAll() {
        return couponRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public CouponResponse getById(Long id) {
        return couponRepository.findById(id).map(this::toResponse)
                .orElseThrow(() -> ApiException.notFound("Coupon not found: " + id));
    }

    @Transactional
    public CouponResponse create(CouponRequest request) {
        String code = normalizeCode(request.getCode());
        if (couponRepository.findByCode(code).isPresent()) {
            throw ApiException.conflict("Coupon code already exists: " + code);
        }
        Coupon coupon = Coupon.builder()
                .code(code)
                .type(parseType(request.getType()))
                .value(defaultMoney(request.getValue()))
                .minOrder(defaultMoney(request.getMinOrder()))
                .expiresAt(request.getExpiresAt())
                .active(request.getActive())
                .build();
        return toResponse(couponRepository.save(coupon));
    }

    @Transactional
    public CouponResponse update(Long id, CouponRequest request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Coupon not found: " + id));
        String code = normalizeCode(request.getCode());
        couponRepository.findByCode(code)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> { throw ApiException.conflict("Coupon code already exists: " + code); });
        coupon.setCode(code);
        coupon.setType(parseType(request.getType()));
        coupon.setValue(defaultMoney(request.getValue()));
        coupon.setMinOrder(defaultMoney(request.getMinOrder()));
        coupon.setExpiresAt(request.getExpiresAt());
        coupon.setActive(request.getActive());
        return toResponse(couponRepository.save(coupon));
    }

    @Transactional
    public void delete(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Coupon not found: " + id));
        couponRepository.delete(coupon);
    }

    public CouponValidationResponse validateCoupon(String code, BigDecimal subtotal) {
        if (code == null || code.isBlank()) {
            return CouponValidationResponse.builder()
                    .valid(false)
                    .message("Coupon code is required")
                    .discountAmount(BigDecimal.ZERO)
                    .finalTotal(subtotal)
                    .build();
        }
        String normalizedCode = normalizeCode(code);
        Coupon coupon = couponRepository.findByCode(normalizedCode)
                .orElseThrow(() -> ApiException.badRequest("Invalid coupon code"));
        if (!coupon.isActive()) {
            return buildInvalidResponse(coupon, "Coupon is inactive", subtotal);
        }
        if (coupon.getExpiresAt().isBefore(Instant.now())) {
            return buildInvalidResponse(coupon, "Coupon has expired", subtotal);
        }
        if (subtotal == null) {
            subtotal = BigDecimal.ZERO;
        }
        if (subtotal.compareTo(coupon.getMinOrder()) < 0) {
            return buildInvalidResponse(coupon, "Order total must be at least " + coupon.getMinOrder(), subtotal);
        }
        BigDecimal discountAmount = calculateDiscount(coupon, subtotal);
        BigDecimal finalTotal = subtotal.subtract(discountAmount);
        return CouponValidationResponse.builder()
                .valid(true)
                .message("Coupon applied successfully")
                .coupon(toResponse(coupon))
                .discountAmount(discountAmount)
                .finalTotal(finalTotal)
                .build();
    }

    public BigDecimal calculateDiscount(Coupon coupon, BigDecimal subtotal) {
        if (coupon.getType() == CouponType.PERCENT) {
            BigDecimal percent = coupon.getValue().divide(BigDecimal.valueOf(100));
            BigDecimal discounted = subtotal.multiply(percent).setScale(2, BigDecimal.ROUND_HALF_UP);
            return discounted.compareTo(subtotal) > 0 ? subtotal : discounted;
        }
        BigDecimal amount = coupon.getValue();
        return amount.compareTo(subtotal) > 0 ? subtotal : amount;
    }

    private CouponResponse toResponse(Coupon coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .type(coupon.getType().name().toLowerCase())
                .value(coupon.getValue())
                .minOrder(coupon.getMinOrder())
                .expiresAt(coupon.getExpiresAt())
                .active(coupon.isActive())
                .build();
    }

    private CouponType parseType(String type) {
        if (type == null) {
            throw ApiException.badRequest("Coupon type is required");
        }
        try {
            return CouponType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw ApiException.badRequest("Coupon type must be percent or flat");
        }
    }

    private String normalizeCode(String code) {
        if (code == null) {
            return null;
        }
        return code.trim().toUpperCase();
    }

    private BigDecimal defaultMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private CouponValidationResponse buildInvalidResponse(Coupon coupon, String message, BigDecimal subtotal) {
        return CouponValidationResponse.builder()
                .valid(false)
                .message(message)
                .coupon(toResponse(coupon))
                .discountAmount(BigDecimal.ZERO)
                .finalTotal(subtotal)
                .build();
    }
}
