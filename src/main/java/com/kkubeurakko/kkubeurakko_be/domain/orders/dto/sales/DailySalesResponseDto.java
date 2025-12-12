package com.kkubeurakko.kkubeurakko_be.domain.orders.dto.sales;

import lombok.Builder;

@Builder
public record DailySalesResponseDto(
	int salesAmount,
	int salesDiff,

	int orderCount,
	int orderCountDiff,

	int refundAmount,
	int discountAmount
) {
}