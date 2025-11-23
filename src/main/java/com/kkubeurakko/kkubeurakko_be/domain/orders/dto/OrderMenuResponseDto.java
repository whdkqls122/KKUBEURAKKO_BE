package com.kkubeurakko.kkubeurakko_be.domain.orders.dto;

import java.util.List;

public record OrderMenuResponseDto(
	String name,
	int quantity,
	int totalPrice,
	List<OrderItemsOptionResponseDto> optionList
) {
}
