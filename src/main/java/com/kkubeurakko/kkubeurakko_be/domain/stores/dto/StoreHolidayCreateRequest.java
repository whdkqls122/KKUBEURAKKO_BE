package com.kkubeurakko.kkubeurakko_be.domain.stores.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;

import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.HolidayType;

import jakarta.validation.constraints.NotNull;

public record StoreHolidayCreateRequest(
	@NotNull HolidayType holidayType,
	LocalDate holidayDate,
	DayOfWeek dayOfWeek
) {
}