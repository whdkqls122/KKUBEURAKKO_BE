package com.kkubeurakko.kkubeurakko_be.domain.stores.service;

import java.time.LocalDate;
import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.StoreBusinessHoursRequest;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.StoreBusinessHoursResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.StoreBusinessHours;

public interface StoreBusinessHoursService {
	void saveBusinessHours(Long storeId, StoreBusinessHoursRequest request);

	//영업 시간 조회
	List<StoreBusinessHoursResponseDto> getBusinessHours(Long storeId);

	//현재 기준 마감시간 조회
	LocalDate resolveBusinessDate(Long storeId);

	StoreBusinessHours getHoursByDate(Long storeId, LocalDate date);
}
