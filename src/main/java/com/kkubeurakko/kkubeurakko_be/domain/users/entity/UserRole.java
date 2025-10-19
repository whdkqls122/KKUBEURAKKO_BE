package com.kkubeurakko.kkubeurakko_be.domain.users.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRole {
	ADMIN("ADMIN", 0),
	CUSTOMER("CUSTOMER", 1);

	private final String role;
	private final int accessLevel;

	public static UserRole findUserRole(String role) {
		switch (role) {
			case "ADMIN":
				return ADMIN;
			case "CUSTOMER":
				return CUSTOMER;
			default:
				throw new RuntimeException("잘못된 권한");
		}
	}
}
