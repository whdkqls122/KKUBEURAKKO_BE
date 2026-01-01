package com.kkubeurakko.kkubeurakko_be.domain.stores.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class StoreUpdateRequest {

	private int minOrderPrice;
	private int deliveryAvgTime;
	private int packagingAvgTime;
}