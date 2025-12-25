package com.kkubeurakko.kkubeurakko_be.domain.stores.dto;

import java.time.LocalDate;

import lombok.Builder;

@Builder
public record DailySalesCalendarDto(
	LocalDate date,
	int salesAmount,
	int refundAmount
) {
}
