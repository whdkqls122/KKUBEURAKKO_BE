package com.kkubeurakko.kkubeurakko_be.domain.menus.dto;

import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuStatus;

import lombok.Builder;

@Builder
public record MenuResponseDto(
	Long menuId,
	String name,
	String description,
	int price,
	MenuStatus menuStatus,
	String img
) {
}
