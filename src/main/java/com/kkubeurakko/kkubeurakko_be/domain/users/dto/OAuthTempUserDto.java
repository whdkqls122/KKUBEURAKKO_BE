package com.kkubeurakko.kkubeurakko_be.domain.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuthTempUserDto implements TempUser {
	private String userNumber;
	private String nickname;
	private String email;

	@Override
	public boolean isOAuth() {
		return true;
	}
}
