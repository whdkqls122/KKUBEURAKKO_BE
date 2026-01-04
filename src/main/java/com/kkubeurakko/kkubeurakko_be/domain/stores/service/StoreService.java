package com.kkubeurakko.kkubeurakko_be.domain.stores.service;

import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.StoreOperationInfoResponse;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.StoreUpdateRequest;

public interface StoreService {
	void updateStoreBasicInfo(Long storeId, StoreUpdateRequest request);

	//가게 운영정보 조회
	StoreOperationInfoResponse getOperationInfo(Long storeId);
}
