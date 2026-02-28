package com.kkubeurakko.kkubeurakko_be.domain.portone.dto;

import lombok.Builder;

@Builder
public record PortOneCashReceiptRequestDto(
	String storeId,
	String paymentId,
	String channelKey,
	CashReceiptType type,
	String orderName,
	String currency,
	PaymentAmountInput amount,
	IssueCashReceiptCustomerInput customer
) {
}
