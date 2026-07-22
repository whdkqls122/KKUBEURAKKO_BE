package com.kkubeurakko.kkubeurakko_be.domain.menus.dto;

import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuStatus;

import lombok.Builder;

@Builder
public record MenuSummaryDto(
	Long id,
	String name,
	String description,
	int price,
	String img,
	MenuStatus menuStatus
) {
}
