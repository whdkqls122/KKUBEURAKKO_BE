package com.kkubeurakko.kkubeurakko_be.domain.users.dto;

import lombok.Builder;

@Builder
public record UserInfoResponseDto(
	String nickname,
	String email,
) {
}
