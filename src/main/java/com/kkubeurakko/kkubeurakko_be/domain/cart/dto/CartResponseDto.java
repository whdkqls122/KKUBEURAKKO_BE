package com.kkubeurakko.kkubeurakko_be.domain.cart.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record CartResponseDto(
	Long cartId,
	List<CartItemResponseDto> cartItemResponseDtoList
) {
}
