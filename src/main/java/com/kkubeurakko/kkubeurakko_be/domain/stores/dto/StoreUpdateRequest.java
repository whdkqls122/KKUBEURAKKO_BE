package com.kkubeurakko.kkubeurakko_be.domain.stores.dto;

import lombok.Builder;

@Builder
public record StoreUpdateRequest(
	Integer minOrderPrice,
	Integer deliveryAvgTime,
	Integer packagingAvgTime
) {
}