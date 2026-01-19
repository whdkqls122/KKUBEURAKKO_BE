package com.kkubeurakko.kkubeurakko_be.domain.stores.service;

import java.time.LocalDate;

import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.OnceHolidayCreateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.WeeklyHolidayCreateRequest;

public interface StoreHolidayService {

	void createOnceHoliday(Long storeId, OnceHolidayCreateRequest request);

	void createWeeklyHoliday(Long storeId, WeeklyHolidayCreateRequest request);

	boolean isHoliday(Long storeId, LocalDate date);
}
