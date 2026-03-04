package com.kkubeurakko.kkubeurakko_be.domain.coupons.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.CouponType;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.Status;

import lombok.Builder;

@Builder
public record IssuedCouponResponseDto(
	Long id,
	String name,
	String couponNumber,
	Integer discountAmount,
	Integer discountRate,
	int minOrderAmount,
	CouponType couponType,
	@JsonFormat(pattern = "yyyy-MM-dd")
	LocalDate validFrom,
	@JsonFormat(pattern = "yyyy-MM-dd")
	LocalDate validUntil,
	Status status
) {
}
