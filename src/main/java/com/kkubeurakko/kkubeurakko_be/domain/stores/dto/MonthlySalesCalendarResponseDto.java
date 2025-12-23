package com.kkubeurakko.kkubeurakko_be.domain.stores.dto;

import java.util.List;

public record MonthlySalesCalendarResponseDto(
	int monthTotalSales,
	int monthTotalRefund,
	List<DailySalesCalendarDto> dailySales
) {
}