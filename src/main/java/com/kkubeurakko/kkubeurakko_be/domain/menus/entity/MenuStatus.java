package com.kkubeurakko.kkubeurakko_be.domain.menus.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MenuStatus {
	ON_SALE("판매중"),
	HIDE("숨김"),
	SOLD_OUT("품절");

	private final String status;
}
