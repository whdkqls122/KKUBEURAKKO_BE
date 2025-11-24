package com.kkubeurakko.kkubeurakko_be.domain.menus.dto;

import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuStatus;

import lombok.Builder;

@Builder
public record AdminMenuDetailDto(
	Long menuId,
	String name,
	String description,
	int price,
	MenuStatus status,
	String img,
	List<OptionGroupSimpleDto> optionGroups
) {
	@Builder
	public record OptionGroupSimpleDto(Long id, String name) {
	}
}
