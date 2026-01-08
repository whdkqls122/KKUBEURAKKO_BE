package com.kkubeurakko.kkubeurakko_be.domain.stores.service;

import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.DeliveryRegionCreateRequest;

public interface DeliveryRegionService {
	//배달 가능 지역 저장
	void saveDeliveryRegion(Long storeId, DeliveryRegionCreateRequest request);
}
