package com.kkubeurakko.kkubeurakko_be.domain.users.dto;

import lombok.Builder;

@Builder
public record LocalTempUserDto(
	String id,
	String password
) {
}
