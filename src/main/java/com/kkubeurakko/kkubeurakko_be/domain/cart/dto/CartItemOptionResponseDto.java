package com.kkubeurakko.kkubeurakko_be.domain.cart.dto;

public record CartItemOptionResponseDto(
	Long optionId,
	String optionName,
	int price
) {
}
