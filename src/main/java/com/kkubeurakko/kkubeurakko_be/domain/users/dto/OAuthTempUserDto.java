package com.kkubeurakko.kkubeurakko_be.domain.users.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class OAuthTempUserDto implements TempUser {
	private final String userNumber;
	private final String nickname;
	private final String email;

	@Override
	public boolean isOAuth() {
		return true;
	}
}
