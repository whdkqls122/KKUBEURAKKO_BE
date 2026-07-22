package com.kkubeurakko.kkubeurakko_be.domain.options.dto;

import lombok.Builder;

@Builder
public record OptionGroupNameResponseDto(
	Long id,
	String name
) {
}
