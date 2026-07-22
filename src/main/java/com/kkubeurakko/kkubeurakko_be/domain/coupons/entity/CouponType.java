package com.kkubeurakko.kkubeurakko_be.domain.coupons.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CouponType {
	DELIVERY("배달"),
	TAKEOUT("포장"),
	ALL("전체");

	private final String type;
}
