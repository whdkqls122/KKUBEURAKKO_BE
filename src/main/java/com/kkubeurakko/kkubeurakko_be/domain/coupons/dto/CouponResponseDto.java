package com.kkubeurakko.kkubeurakko_be.domain.coupons.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
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
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	LocalDate validFrom,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	LocalDate validUntil,
	int quantity,
	Status status
) {
}
