package com.kkubeurakko.kkubeurakko_be.domain.stores.dto;

import java.time.LocalDate;

import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.HolidayType;

import jakarta.validation.constraints.NotNull;

public record OnceHolidayCreateRequest(
	@NotNull HolidayType holidayType,
	@NotNull(message = "휴무 날짜는 필수입니다.")
	LocalDate holidayDate
) {
}