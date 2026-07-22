package com.kkubeurakko.kkubeurakko_be.domain.stores.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.DeliveryRegionAbleResponse;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.DeliveryRegionRequest;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.DeliveryRegionResponse;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.DeliveryRegion;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.DeliveryRegionRepository;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.StoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeliveryRegionServiceImpl implements DeliveryRegionService {

	private final DeliveryRegionRepository deliveryRegionRepository;
	private final StoreRepository storeRepository;
	private static final double EARTH_RADIUS = 6371000;

	@Override
	public void saveDeliveryRegion(Long storeId, DeliveryRegionRequest request) {

		Stores store = storeRepository.findById(storeId)
			.orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다."));

		DeliveryRegion deliveryRegion = DeliveryRegion.builder()
			.store(store)
			.distance(request.distance())
			.build();

		deliveryRegionRepository.save(deliveryRegion);
	}

	@Transactional
	@Override
	public void updateDeliveryRegion(
		Long storeId,
		DeliveryRegionRequest request
	) {
		DeliveryRegion deliveryRegion = deliveryRegionRepository.findByStoreId(storeId)
			.orElseThrow(() -> new IllegalArgumentException("배달 지역을 찾을 수 없습니다."));

		deliveryRegion.updateDistance(request.distance());
	}

	@Override
	public DeliveryRegionResponse getDeliveryRegion(Long storeId) {
		Stores store = storeRepository.findById(storeId).orElseThrow(
			() -> new IllegalArgumentException("가게 정보를 찾을 수 없습니다.")
		);
		DeliveryRegion deliveryRegion = deliveryRegionRepository.findByStoreId(storeId).orElseThrow(
			() -> new IllegalArgumentException("배달 가능 지역 정보를 찾을 수 없습니다.")
		);

		return DeliveryRegionResponse.builder()
			.storeLat(store.getLatitude())
			.storeLng(store.getLongitude())
			.distance(deliveryRegion.getDistance())
			.build();
	}

	@Transactional(readOnly = true)
	public DeliveryRegionAbleResponse isDeliverable(Long storeId, double latitude, double longitude) {
		Stores store = storeRepository.findById(storeId).orElseThrow(
			() -> new IllegalArgumentException("가게 정보를 찾을 수 없습니다.")
		);
		DeliveryRegion deliveryRegion = deliveryRegionRepository.findByStoreId(storeId).orElseThrow(
			() -> new IllegalArgumentException("배달 가능 지역 정보를 찾을 수 없습니다.")
		);

		double distance = calculateDistanceInMeters(
			store.getLatitude(),
			store.getLongitude(),
			latitude,
			longitude
		);
		double maxDistance = deliveryRegion.getDistance() * 1000;

		boolean able = distance <= maxDistance;

		return DeliveryRegionAbleResponse.builder()
			.able(able)
			.build();
	}

	private double calculateDistanceInMeters(
		double lat1, double lng1,
		double lat2, double lng2
	) {
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);

		double radLat1 = Math.toRadians(lat1);
		double radLat2 = Math.toRadians(lat2);

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
			Math.cos(radLat1) * Math.cos(radLat2) *
				Math.sin(dLng / 2) * Math.sin(dLng / 2);

		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		return EARTH_RADIUS * c;
	}
}