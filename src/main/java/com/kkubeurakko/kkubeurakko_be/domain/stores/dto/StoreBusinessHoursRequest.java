package com.kkubeurakko.kkubeurakko_be.domain.stores.dto;

import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.OperationType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record StoreBusinessHoursRequest(
	@NotNull(message = "설정 타입은 필수입니다.")
	OperationType operationType,
	@NotEmpty(message = "영업 시간 데이터는 최소 1개 이상 필요합니다.")
	@Valid
	List<BusinessTimeDto> commonTime
) {
}