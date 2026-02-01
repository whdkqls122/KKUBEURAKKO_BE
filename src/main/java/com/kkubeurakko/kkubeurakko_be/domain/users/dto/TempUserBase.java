package com.kkubeurakko.kkubeurakko_be.domain.users.dto;

import lombok.Builder;

@Builder
public record TempUserBase(
	String userNumber,
	String nickname
) {
}
