package com.kkubeurakko.kkubeurakko_be.domain.stores.dto;

import java.time.LocalTime;

import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.BusinessDay;

public record BusinessTimeDto(
	BusinessDay dayOfWeek,
	LocalTime openTime,
	LocalTime closeTime,
	boolean hasBreakTime,
	LocalTime breakStartTime,
	LocalTime breakEndTime
) {
}