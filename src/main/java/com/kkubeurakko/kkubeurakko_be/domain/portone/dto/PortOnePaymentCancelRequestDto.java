package com.kkubeurakko.kkubeurakko_be.domain.portone.dto;

import lombok.Builder;

@Builder
public record PortOnePaymentCancelRequestDto(
	String reason
) {
}
