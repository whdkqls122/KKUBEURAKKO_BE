package com.kkubeurakko.kkubeurakko_be.domain.coupons.dto;

import java.time.LocalDate;

import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.CouponType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CouponCreateRequest(
	@NotBlank String name,
	Integer discountAmount,
	Integer discountRate,
	@Min(0) int minOrderAmount,
	@NotNull CouponType couponType,
	@NotNull LocalDate validFrom,
	@NotNull LocalDate validUntil,
	@Min(1) int quantity
) {
}