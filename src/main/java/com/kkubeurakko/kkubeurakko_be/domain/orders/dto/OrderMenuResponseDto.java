package com.kkubeurakko.kkubeurakko_be.domain.orders.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record OrderMenuResponseDto(
	String name,
	int quantity,
	int totalPrice,
	List<OrderItemsOptionResponseDto> optionList
) {
}
