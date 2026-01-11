package com.kkubeurakko.kkubeurakko_be.domain.stores.service;

import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.StoreBusinessHoursRequest;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.StoreBusinessHoursResponseDto;

public interface StoreBusinessHoursService {
	void saveBusinessHours(Long storeId, StoreBusinessHoursRequest request);

	//영업 시간 조회
	List<StoreBusinessHoursResponseDto> getBusinessHours(Long storeId);

}
