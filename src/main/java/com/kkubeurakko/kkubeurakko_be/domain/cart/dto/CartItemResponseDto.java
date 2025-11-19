package com.kkubeurakko.kkubeurakko_be.domain.cart.dto;

import java.util.List;

public record CartItemResponseDto(
	Long cartItemId,
	Long menuId,
	String menuName,
	int quantity,
	int totalPrice,
	List<CartItemOptionResponseDto> options
) {
}
