package com.kkubeurakko.kkubeurakko_be.domain.stores.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public record StorePauseRequest(
	@NotNull LocalDateTime startDateTime,
	@NotNull LocalDateTime endDateTime
) {
}