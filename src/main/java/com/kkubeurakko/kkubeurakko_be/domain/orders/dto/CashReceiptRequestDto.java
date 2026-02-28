package com.kkubeurakko.kkubeurakko_be.domain.orders.dto;

import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.CashReceiptType;

public record CashReceiptRequestDto(
	CashReceiptType type,
	String identityNumber
) {
}
