package com.kkubeurakko.kkubeurakko_be.domain.options.dto;

import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroupType;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.Status;

import lombok.Builder;

@Builder
public record OptionGroupResponseDto(
	Long id,
	String name,
	String description,
	boolean required,
	OptionGroupType optionGroupType,
	int selectedNum,
	Status status,
	List<OptionResponseDto> options
) {
}