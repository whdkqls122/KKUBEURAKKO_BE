package com.kkubeurakko.kkubeurakko_be.domain.stores.dto;

import jakarta.validation.constraints.NotNull;

public record StoreOperationCreateRequest(

	@NotNull
	Integer minOrderPrice,

	@NotNull
	Integer deliveryAvgTime,

	@NotNull
	Integer packagingAvgTime

) {
}