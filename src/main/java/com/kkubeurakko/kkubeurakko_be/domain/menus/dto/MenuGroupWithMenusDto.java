package com.kkubeurakko.kkubeurakko_be.domain.menus.dto;

import java.util.List;

public record MenuGroupWithMenusDto(
	Long id,
	String name,
	String description,
	String status,
	List<MenuResponseDto> menus
) {
}
