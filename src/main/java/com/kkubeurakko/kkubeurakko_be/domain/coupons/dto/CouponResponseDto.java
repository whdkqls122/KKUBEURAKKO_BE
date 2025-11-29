package com.kkubeurakko.kkubeurakko_be.domain.coupons.dto;

import java.time.LocalDate;

import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.CouponType;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.Status;

import lombok.Builder;

@Builder
public record CouponResponseDto(
	Long id,
	String name,
	String discountAmount,
	int minOrderAmount,
	CouponType couponType,
	LocalDate validFrom,
	LocalDate validUntil,
	int quantity,
	Status status
) {
}
