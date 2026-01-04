package com.kkubeurakko.kkubeurakko_be.domain.stores.dto;

import java.time.LocalTime;

import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.BusinessDay;

import jakarta.validation.constraints.NotNull;

public record BusinessTimeDto(
	@NotNull(message = "요일은 필수입니다.")
	BusinessDay dayOfWeek,
	@NotNull(message = "오픈 시간은 필수입니다.")
	LocalTime openTime,
	@NotNull(message = "마감 시간은 필수입니다.")
	LocalTime closeTime,
	@NotNull(message = "브레이크 여부는 필수입니다.")
	boolean hasBreakTime,
	LocalTime breakStartTime,
	LocalTime breakEndTime
) {
}