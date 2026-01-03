package com.kkubeurakko.kkubeurakko_be.domain.cart.dto;

import lombok.Builder;

@Builder
public record CartItemQuantityResponseDto(
	int cartItemQuantity
) {
}
