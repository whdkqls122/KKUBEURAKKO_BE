package com.kkubeurakko.kkubeurakko_be.domain.payments.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PaymentType {
	GENERAL("일반결제"),
	CARD("카드결제"),
	EASY_PAY("간편결제"),
	CONVENIENCE_STORE("편의점 결제"),
	GIFT_CERTIFICATE("상품권 결제"),
	MOBILE("휴대폰 결제"),
	TRANSFER("계좌이체"),
	VIRTUAL_ACCOUNT("가상계좌");

	private final String type;
}
