package com.kkubeurakko.kkubeurakko_be.domain.stores.dto;

import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.StoreStatus;

public record StoreStatusResponse(
	StoreStatus status
) {
}