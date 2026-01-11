package com.kkubeurakko.kkubeurakko_be.domain.stores.service;

import org.springframework.stereotype.Service;

import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.DeliveryRegionCreateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.DeliveryRegionStatusUpdateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.DeliveryRegion;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.DeliveryRegionRepository;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.StoreRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeliveryRegionServiceImpl implements DeliveryRegionService {

	private final DeliveryRegionRepository deliveryRegionRepository;
	private final StoreRepository storeRepository;

	@Override
	public void saveDeliveryRegion(Long storeId, DeliveryRegionCreateRequest request) {

		Stores store = storeRepository.findById(storeId)
			.orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다."));

		DeliveryRegion deliveryRegion = DeliveryRegion.builder()
			.store(store)
			.addressName(request.addressName())
			.isAvailable(true) //처음 저장시 기본 true로 저장
			.build();

		deliveryRegionRepository.save(deliveryRegion);
	}

	@Transactional
	@Override
	public void updateDeliveryRegionStatus(
		Long storeId,
		DeliveryRegionStatusUpdateRequest request
	) {
		DeliveryRegion deliveryRegion = deliveryRegionRepository.findById(request.deliveryRegionId())
			.orElseThrow(() -> new IllegalArgumentException("배달 지역을 찾을 수 없습니다."));

		deliveryRegion.changeAvailability(request.isAvailable());
	}
}