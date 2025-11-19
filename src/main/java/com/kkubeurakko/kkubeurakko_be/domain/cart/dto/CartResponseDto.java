package com.kkubeurakko.kkubeurakko_be.domain.cart.dto;

import java.util.List;

public record CartResponseDto(
	Long cartId,
	List<CartItemResponseDto> items
) {
}
