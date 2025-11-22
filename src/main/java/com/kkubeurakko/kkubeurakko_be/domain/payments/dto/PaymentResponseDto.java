package com.kkubeurakko.kkubeurakko_be.domain.payments.dto;

import com.kkubeurakko.kkubeurakko_be.domain.payments.entity.PaymentType;

public record PaymentResponseDto(
	int totalAmount,
	int finalAmount,
	PaymentType paymentType,
	int disCountAmount
) {
}
