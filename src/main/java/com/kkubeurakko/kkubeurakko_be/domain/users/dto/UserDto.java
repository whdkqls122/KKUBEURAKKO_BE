package com.kkubeurakko.kkubeurakko_be.domain.users.dto;

import com.kkubeurakko.kkubeurakko_be.domain.users.entity.UserRole;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class UserDto {
	private UserRole role;
	private String userNumber;
	private boolean firstLogin;
}
