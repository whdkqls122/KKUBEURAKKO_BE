package com.kkubeurakko.kkubeurakko_be.domain.portone.dto;

import lombok.Builder;

@Builder
public record IssueCashReceiptCustomerInput(
	String identityNumber,
	String name,
	String email
) {
}
