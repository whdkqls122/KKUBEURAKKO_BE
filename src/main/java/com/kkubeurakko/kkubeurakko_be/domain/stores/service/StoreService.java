package com.kkubeurakko.kkubeurakko_be.domain.stores.service;

import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.StoreOperationCreateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.StoreOperationInfoResponse;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.StoreStatusResponse;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.StoreUpdateRequest;

public interface StoreService {
	//운영정보 저장
	void createOperationInfo(Long storeId, StoreOperationCreateRequest request);

	void updateStoreBasicInfo(Long storeId, StoreUpdateRequest request);

	//가게 운영정보 조회
	StoreOperationInfoResponse getOperationInfo(Long storeId);

	//가게 영업 일시 중지
	void pauseStore(Long storeId);

	//가게 상태 조회
	StoreStatusResponse getStoreStatus(Long storeId);
}
