package com.kkubeurakko.kkubeurakko_be.domain.stores.service;

import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.DeliveryRegionCreateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.DeliveryRegionStatusUpdateRequest;

public interface DeliveryRegionService {
	//배달 가능 지역 저장
	void saveDeliveryRegion(Long storeId, DeliveryRegionCreateRequest request);

	//배달 가능 지역 상태 변경
	void updateDeliveryRegionStatus(Long storeId, DeliveryRegionStatusUpdateRequest request
	);
}
