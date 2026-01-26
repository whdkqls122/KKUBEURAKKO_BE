package com.kkubeurakko.kkubeurakko_be.domain.users.dto;

import lombok.Builder;

@Builder
public record TempUserDto(
	String userNumber,
	String email,
	String nickname
) {
}
