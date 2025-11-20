package com.kkubeurakko.kkubeurakko_be.domain.menus.dto;

import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuStatus;

import lombok.Builder;

@Builder
public record MenuGroupWithMenusDto(
	Long id,
	String name,
	String description,
	MenuStatus menuStatus,
	List<MenuSummaryDto> menus
) {
}
