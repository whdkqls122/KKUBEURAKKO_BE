package com.kkubeurakko.kkubeurakko_be.domain.orders.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Status {
	PENDING("접수 대기"),
	ACCEPTED("접수"),
	COOKING("조리 중"),
	ON_DELIVERY("배달 중"),
	COMPLETED("배달 완료"),
	CANCELED("취소"),
	REFUNDED("환불");

	private final String status;
}
