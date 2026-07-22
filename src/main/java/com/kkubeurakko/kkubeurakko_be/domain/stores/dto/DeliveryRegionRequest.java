package com.kkubeurakko.kkubeurakko_be.domain.stores.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DeliveryRegionRequest(
	@NotNull(message = "배달 반경 거리는 필수입니다.")
	@Min(value = 0, message = "배달 가능 반경은 0km 이상이어야 합니다.")
	double distance
) {
}