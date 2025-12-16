package com.kkubeurakko.kkubeurakko_be.domain.portone.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PortOneVerifyInfoResponseDto(
	String status,
	String id,
	VerifiedCustomer verifiedCustomer
) {
}
