package com.kkubeurakko.kkubeurakko_be.domain.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocalTempUserDto implements TempUser {
	private String userNumber;
	private String id;
	private String email;
	private String nickname;
	private String passwordHash;

	@Override
	public boolean isOAuth() {
		return false;
	}
}
