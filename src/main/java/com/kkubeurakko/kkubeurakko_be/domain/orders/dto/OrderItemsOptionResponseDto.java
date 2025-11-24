package com.kkubeurakko.kkubeurakko_be.domain.orders.dto;

import lombok.Builder;

@Builder
public record OrderItemsOptionResponseDto(
	String name,
	int price
) {
}
