package com.kkubeurakko.kkubeurakko_be.domain.portone.dto;

public record PaymentAmount(
	int total,
	int taxFree,
	int vat,
	int supply,
	int discount
) {
}
