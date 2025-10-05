package com.kkubeurakko.kkubeurakko_be.domain.users.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRole {
	ADMIN("관리자", 0),
	CUSTOMER("회원", 1);

	private final String role;
	private final int accessLevel;

	public static UserRole findUserRole(String role) {
		switch (role) {
			case "관리자":
				return ADMIN;
			case "회원":
				return CUSTOMER;
			default:
				throw new RuntimeException("잘못된 권한");
		}
	}
}
