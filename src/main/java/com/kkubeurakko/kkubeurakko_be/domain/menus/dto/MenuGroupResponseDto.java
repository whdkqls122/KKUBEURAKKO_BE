package com.kkubeurakko.kkubeurakko_be.domain.menus.dto;

import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuStatus;

import lombok.Builder;

@Builder
public record MenuGroupResponseDto(
	Long menuGroupId,
	String name,
	MenuStatus menuGroupStatus
) {
}
