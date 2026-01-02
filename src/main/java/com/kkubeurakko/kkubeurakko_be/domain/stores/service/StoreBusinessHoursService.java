package com.kkubeurakko.kkubeurakko_be.domain.stores.service;

import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.StoreBusinessHoursRequest;

public interface StoreBusinessHoursService {
	void saveBusinessHours(Long storeId, StoreBusinessHoursRequest request);
}
