package com.kkubeurakko.kkubeurakko_be.domain.orders.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderType {
	DELIVERY("배달", "A"),
	TAKEOUT("포장", "B");
	private final String type;
	private final String code;
}
