package com.kkubeurakko.kkubeurakko_be.domain.portone.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)

public record VerifiedCustomer(
	@JsonProperty("phoneNumber")
	String phoneNumber
) {
}
