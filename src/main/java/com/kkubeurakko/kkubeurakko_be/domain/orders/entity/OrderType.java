package com.kkubeurakko.kkubeurakko_be.domain.orders.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderType {
	DELIVERY("배달"),
	TAKEOUT("포장");
	private final String type;
}
