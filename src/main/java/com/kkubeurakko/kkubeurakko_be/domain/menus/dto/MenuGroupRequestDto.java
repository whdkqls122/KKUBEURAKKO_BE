package com.kkubeurakko.kkubeurakko_be.domain.menus.dto;

import lombok.Builder;

@Builder
public record MenuGroupRequestDto(
	String name,
	String description
) {
}
