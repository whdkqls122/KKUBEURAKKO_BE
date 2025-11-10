package com.kkubeurakko.kkubeurakko_be.domain.menus.dto;

import lombok.Builder;

@Builder
public record MenuGroupUpdateRequestDto(
	Long menuGroupId,
	String name,
	String description
) {
}
