package com.kkubeurakko.kkubeurakko_be.domain.portone.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PortOnePaymentResponseDto(
	String status,
	String id,
	String transactionId,
	String storeId,
	PaymentAmount amount,
	PaymentMethodDto method
) {
}
