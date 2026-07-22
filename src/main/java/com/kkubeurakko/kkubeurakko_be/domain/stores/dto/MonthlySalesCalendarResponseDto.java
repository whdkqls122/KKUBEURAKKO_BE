package com.kkubeurakko.kkubeurakko_be.domain.stores.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record MonthlySalesCalendarResponseDto(
	int monthTotalSales,
	int monthTotalRefund,
	List<DailySalesCalendarDto> dailySales
) {
}