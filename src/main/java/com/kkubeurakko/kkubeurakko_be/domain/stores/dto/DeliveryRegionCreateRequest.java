package com.kkubeurakko.kkubeurakko_be.domain.stores.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeliveryRegionCreateRequest(
	@NotBlank(message = "배달 지역명은 필수입니다.")
	@Size(max = 60, message = "배달 지역명은 최대 60자까지 가능합니다.")
	String addressName
) {
}