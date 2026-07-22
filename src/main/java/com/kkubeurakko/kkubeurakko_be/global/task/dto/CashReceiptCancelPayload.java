package com.kkubeurakko.kkubeurakko_be.global.task.dto;

import lombok.Builder;

@Builder
public record CashReceiptCancelPayload(
	String paymentId
) {
}
