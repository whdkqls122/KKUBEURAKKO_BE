package com.kkubeurakko.kkubeurakko_be.domain.options.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Status {
	ON_SALE("판매중"),
	HIDE("숨김");

	private final String status;
}
