package com.kkubeurakko.kkubeurakko_be.domain.menus.dto;

import lombok.Builder;

@Builder
public record MenuGroupResponseDto(
	Long menuGroupId,
	String name
) {
}
