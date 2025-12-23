package com.kkubeurakko.kkubeurakko_be.domain.stores.dto;

import java.time.LocalDate;

public record DailySalesCalendarDto(
	LocalDate date,
	int salesAmount,
	int refundAmount
) {
}
