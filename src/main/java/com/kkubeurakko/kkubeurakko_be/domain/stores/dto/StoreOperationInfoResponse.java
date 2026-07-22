package com.kkubeurakko.kkubeurakko_be.domain.stores.dto;

import java.time.LocalTime;

import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.StoreStatus;

import lombok.Builder;

@Builder
public record StoreOperationInfoResponse(
	StoreStatus status,
	LocalTime openTime,
	LocalTime closeTime,
	int minOrderPrice,
	int deliveryAvgTime,
	int packagingAvgTime
) {
}
