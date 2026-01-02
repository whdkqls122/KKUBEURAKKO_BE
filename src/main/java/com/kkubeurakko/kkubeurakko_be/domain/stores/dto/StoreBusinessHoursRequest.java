package com.kkubeurakko.kkubeurakko_be.domain.stores.dto;

import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.OperationType;

public record StoreBusinessHoursRequest(
	OperationType operationType,
	List<BusinessTimeDto> commonTime
) {
}