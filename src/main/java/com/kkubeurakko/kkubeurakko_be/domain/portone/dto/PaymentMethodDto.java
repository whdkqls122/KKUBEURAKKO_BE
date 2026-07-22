package com.kkubeurakko.kkubeurakko_be.domain.portone.dto;

import java.util.Map;

public record PaymentMethodDto(
	String type,
	Map<String, Object> details
) {
}
