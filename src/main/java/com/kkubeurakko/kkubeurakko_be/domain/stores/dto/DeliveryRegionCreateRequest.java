package com.kkubeurakko.kkubeurakko_be.domain.stores.dto;

import jakarta.validation.constraints.NotBlank;

public record DeliveryRegionCreateRequest(
	@NotBlank(message = "배달 지역명은 필수입니다.")
	String addressName
) {
}