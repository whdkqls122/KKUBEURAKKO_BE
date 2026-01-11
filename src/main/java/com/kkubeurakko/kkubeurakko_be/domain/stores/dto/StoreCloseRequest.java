package com.kkubeurakko.kkubeurakko_be.domain.stores.dto;

import jakarta.validation.constraints.NotNull;

public record StoreCloseRequest(
	@NotNull(message = "영업 상태 값은 필수입니다.")
	Boolean isOpen
) {
}