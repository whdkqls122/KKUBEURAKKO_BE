package com.kkubeurakko.kkubeurakko_be.domain.users.dto;

import lombok.Builder;

@Builder
public record UserInfoResponseDto(
	String provider,
	String nickname,
	String email
) {
}
