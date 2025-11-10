package com.kkubeurakko.kkubeurakko_be.domain.menus.dto;

import lombok.Builder;

@Builder
public record MenuResponseDto(
	Long menuId,
	String name,
	String description,
	int price,
	String img
) {
}
