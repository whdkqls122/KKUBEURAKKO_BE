package com.kkubeurakko.kkubeurakko_be.domain.options.dto;

import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionStatus;

import lombok.Builder;

@Builder
public record OptionResponseDto(
	Long id,
	String name,
	int price,
	OptionStatus optionStatus
) {
}