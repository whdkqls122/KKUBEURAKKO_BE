package com.kkubeurakko.kkubeurakko_be.domain.stores.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public record StorePauseRequest(
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	LocalDateTime startDateTime,

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	LocalDateTime endDateTime
) {
}
