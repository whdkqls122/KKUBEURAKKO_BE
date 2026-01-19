package com.kkubeurakko.kkubeurakko_be.domain.stores.dto;

import java.time.DayOfWeek;

import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.HolidayType;

import jakarta.validation.constraints.NotNull;

public record WeeklyHolidayCreateRequest(
	@NotNull HolidayType holidayType,
	@NotNull(message = "요일은 필수입니다.")
	DayOfWeek dayOfWeek
) {
}
