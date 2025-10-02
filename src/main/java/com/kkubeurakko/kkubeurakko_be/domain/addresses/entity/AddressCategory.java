package com.kkubeurakko.kkubeurakko_be.domain.addresses.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AddressCategory {
	HOME("집"),
	COMPANY("회사"),
	INPUT("직접 입력");

	private final String category;
}
