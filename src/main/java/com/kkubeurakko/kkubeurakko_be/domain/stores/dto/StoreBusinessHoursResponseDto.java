package com.kkubeurakko.kkubeurakko_be.domain.stores.dto;

import java.time.LocalTime;

import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.BusinessDay;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.OperationType;

import lombok.Builder;

@Builder
public record StoreBusinessHoursResponseDto(
	OperationType operationType,
	BusinessDay dayOfWeek,
	LocalTime openTime,
	LocalTime closeTime,
	boolean hasBreakTime,
	LocalTime breakStartTime,
	LocalTime breakEndTime
) {
}