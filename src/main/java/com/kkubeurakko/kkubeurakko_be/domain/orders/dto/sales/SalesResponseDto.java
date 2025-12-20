package com.kkubeurakko.kkubeurakko_be.domain.orders.dto.sales;

import lombok.Builder;

@Builder
public record SalesResponseDto(
	Long salesAmount,
	Long salesDiff,

	Long orderCount,
	Long orderCountDiff,

	Long refundAmount,
	Long discountAmount
) {
}