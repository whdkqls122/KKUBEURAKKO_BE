package com.kkubeurakko.kkubeurakko_be.domain.stores.service;

import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.DeliveryRegionAbleResponse;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.DeliveryRegionRequest;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.DeliveryRegionResponse;

public interface DeliveryRegionService {
	//배달 가능 지역 저장
	void saveDeliveryRegion(Long storeId, DeliveryRegionRequest request);

	//배달 가능 지역 상태 변경
	void updateDeliveryRegion(Long storeId, DeliveryRegionRequest request);

	//배달 지역 조회
	DeliveryRegionResponse getDeliveryRegion(Long storeId);

	//배달 가능 지역 여부 확인
	DeliveryRegionAbleResponse isDeliverable(Long storeId, double latitude, double longitude);
}
