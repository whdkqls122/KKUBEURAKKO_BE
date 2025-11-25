package com.kkubeurakko.kkubeurakko_be.domain.payments.dto;

import com.kkubeurakko.kkubeurakko_be.domain.payments.entity.PaymentType;
import com.kkubeurakko.kkubeurakko_be.domain.payments.entity.Payments;

import lombok.Builder;

@Builder
public record PaymentResponseDto(
	int totalAmount,
	int finalAmount,
	PaymentType paymentType,
	int disCountAmount
) {
	public static PaymentResponseDto from(Payments payment) {
		return PaymentResponseDto.builder()
			.totalAmount(payment.getTotalAmount())
			.disCountAmount(payment.getDiscountAmount())
			.finalAmount(payment.getFinalAmount())
			.paymentType(payment.getPaymentsType())
			.build();
	}
}
