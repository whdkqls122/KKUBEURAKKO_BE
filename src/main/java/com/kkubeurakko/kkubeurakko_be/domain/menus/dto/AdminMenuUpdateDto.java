package com.kkubeurakko.kkubeurakko_be.domain.menus.dto;

import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuStatus;

import lombok.Builder;

@Builder
public record AdminMenuUpdateDto(
	String name,
	String description,
	int price,
	MenuStatus status,
	String img,
	List<Long> optionGroupIds
) {
}
