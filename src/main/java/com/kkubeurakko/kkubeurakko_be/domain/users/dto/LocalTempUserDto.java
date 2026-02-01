package com.kkubeurakko.kkubeurakko_be.domain.users.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LocalTempUserDto implements TempUser {
	private final String userNumber;
	private final String nickname;
	private final String passwordHash;

	@Override
	public boolean isOAuth() {
		return false;
	}
}
