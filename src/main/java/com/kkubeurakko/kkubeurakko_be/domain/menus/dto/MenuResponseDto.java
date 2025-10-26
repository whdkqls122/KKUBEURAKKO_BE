package com.kkubeurakko.kkubeurakko_be.domain.menus.dto;

public record MenuResponseDto(
	Long menu_id,
	String name,
	String description,
	String price,
	String img
) {
}
