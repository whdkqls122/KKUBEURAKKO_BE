package com.kkubeurakko.kkubeurakko_be.domain.stores.service;

import java.time.LocalDate;
import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.StoreHolidayCreateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.StoreHolidayResponse;

public interface StoreHolidayService {
	void createHoliday(Long storeId, StoreHolidayCreateRequest request);

	List<StoreHolidayResponse> getHolidays(Long storeId);

	boolean isHoliday(Long storeId, LocalDate date);
}
