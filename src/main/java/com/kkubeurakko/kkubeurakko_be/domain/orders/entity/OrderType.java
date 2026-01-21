package com.kkubeurakko.kkubeurakko_be.domain.orders.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderType {
	DELIVERY("배달", "D"),
	TAKEOUT("포장", "T");
	private final String type;
	private final String code;
}
