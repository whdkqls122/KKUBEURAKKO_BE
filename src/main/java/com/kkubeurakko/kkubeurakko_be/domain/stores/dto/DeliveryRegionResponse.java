package com.kkubeurakko.kkubeurakko_be.domain.stores.dto;

import lombok.Builder;

@Builder
public record DeliveryRegionResponse(
	double storeLat,
	double storeLng,
	double distance
) {
}
