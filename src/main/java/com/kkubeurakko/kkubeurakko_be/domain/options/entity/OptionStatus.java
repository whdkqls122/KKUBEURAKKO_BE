package com.kkubeurakko.kkubeurakko_be.domain.options.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OptionStatus {
	ON_SALE("판매중"),
	HIDE("숨김"),
	DELETED("삭제");

	private final String status;
}
