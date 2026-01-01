package com.kkubeurakko.kkubeurakko_be.domain.stores.service;

import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.StoreUpdateRequest;

public interface StoreService {
	void updateStoreBasicInfo(Long storeId, StoreUpdateRequest request);
}
