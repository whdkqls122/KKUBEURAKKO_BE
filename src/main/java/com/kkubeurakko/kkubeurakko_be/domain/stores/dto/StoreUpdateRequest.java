package com.kkubeurakko.kkubeurakko_be.domain.stores.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StoreUpdateRequest {

	private int minOrderPrice;
	private int deliveryAvgTime;
	private int packagingAvgTime;
}