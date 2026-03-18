package com.kkubeurakko.kkubeurakko_be.domain.stores.dto;

import java.time.LocalTime;

import lombok.Builder;

@Builder
public record StoreOperationInfoResponse(
	boolean isOpen,
	LocalTime openTime,
	LocalTime closeTime,
	int minOrderPrice,
	int deliveryAvgTime,
	int packagingAvgTime
) {
}
