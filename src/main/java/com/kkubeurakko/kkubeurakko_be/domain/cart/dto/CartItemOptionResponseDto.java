package com.kkubeurakko.kkubeurakko_be.domain.cart.dto;

import lombok.Builder;

@Builder
public record CartItemOptionResponseDto(
	Long optionId,
	String optionName,
	int price
) {
}
