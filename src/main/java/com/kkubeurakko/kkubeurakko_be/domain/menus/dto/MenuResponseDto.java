package com.kkubeurakko.kkubeurakko_be.domain.menus.dto;

import lombok.Builder;

@Builder
public record MenuResponseDto(
	Long menu_id,
	String name,
	String description,
	String price,
	String img
) {
}
