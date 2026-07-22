package com.kkubeurakko.kkubeurakko_be.domain.stores.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;

import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.HolidayType;

import lombok.Builder;

@Builder
public record StoreHolidayResponse(
	HolidayType holidayType,
	LocalDate holidayDate,
	DayOfWeek dayOfWeek
) {
}