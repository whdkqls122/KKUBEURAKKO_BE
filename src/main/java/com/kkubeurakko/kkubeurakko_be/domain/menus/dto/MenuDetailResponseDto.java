package com.kkubeurakko.kkubeurakko_be.domain.menus.dto;

import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionGroupResponseDto;

import lombok.Builder;

@Builder
public record MenuDetailResponseDto(
	Long menuId,
	String name,
	String description,
	int price,
	String img,
	List<OptionGroupResponseDto> optionGroupResponseDtoList
) {
}
