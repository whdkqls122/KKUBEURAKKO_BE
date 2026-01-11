package com.kkubeurakko.kkubeurakko_be.domain.stores.dto;

public record DeliveryRegionResponseDto(
	Long deliveryRegionId,
	String addressName,
	boolean isAvailable
) {
}