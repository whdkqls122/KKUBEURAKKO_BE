package com.kkubeurakko.kkubeurakko_be.domain.auth.dto;

import lombok.Builder;

@Builder
public record UserIdResponseDto(
	String id
) {
}
