package com.kkubeurakko.kkubeurakko_be.domain.stores.dto;

import lombok.Builder;

@Builder
public record StoreUpdateRequest(

	int minOrderPrice,
	int deliveryAvgTime,
	int packagingAvgTime
) {
}