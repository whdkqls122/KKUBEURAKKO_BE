package com.kkubeurakko.kkubeurakko_be.domain.payments.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PaymentType {
	GENERAL("일반결제"),
	KAKAOPAY("카카오페이"),
	NAVERPAY("네이버페이");

	private final String type;
}
