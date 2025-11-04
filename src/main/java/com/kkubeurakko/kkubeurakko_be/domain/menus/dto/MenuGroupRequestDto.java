package com.kkubeurakko.kkubeurakko_be.domain.menus.dto;

import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.Status;

import lombok.Builder;

@Builder
public record MenuGroupRequestDto(
	String name,
	String description,
	Status status
) {
}
