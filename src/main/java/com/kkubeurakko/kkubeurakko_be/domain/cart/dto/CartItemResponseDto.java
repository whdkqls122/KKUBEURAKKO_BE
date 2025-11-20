package com.kkubeurakko.kkubeurakko_be.domain.cart.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record CartItemResponseDto(
	Long cartItemId,
	Long menuId,
	String menuName,
	int quantity,
	int totalPrice,
	List<CartItemOptionResponseDto> optionResponseDtoList
) {
}
