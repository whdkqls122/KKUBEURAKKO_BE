package com.kkubeurakko.kkubeurakko_be.domain.stores.dto;

import jakarta.validation.constraints.NotNull;

public record DeliveryRegionStatusUpdateRequest(
	@NotNull(message = "배달 가능 여부는 필수입니다.")
	Boolean isAvailable
) {
}