package com.kkubeurakko.kkubeurakko_be.domain.stores.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.OnceHolidayCreateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.StoreHolidayResponse;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.WeeklyHolidayCreateRequest;

public interface StoreHolidayService {

	void createOnceHoliday(Long storeId, OnceHolidayCreateRequest request);

	void createWeeklyHoliday(Long storeId, WeeklyHolidayCreateRequest request);

	boolean isHoliday(Long storeId, LocalDate date);

	List<StoreHolidayResponse> getHolidays(Long storeId);

	//단일 휴무 삭제
	void deleteOnceHoliday(Long storeId, LocalDate holidayDate);

	//반복 휴무 삭제
	void deleteWeeklyHoliday(Long storeId, DayOfWeek dayOfWeek);
}
