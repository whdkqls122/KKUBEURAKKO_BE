package com.kkubeurakko.kkubeurakko_be.domain.stores.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.DeliveryRegionCreateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.DeliveryRegionResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.DeliveryRegionStatusUpdateRequest;
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

	@Override
	public void saveDeliveryRegion(Long storeId, DeliveryRegionCreateRequest request) {

		Stores store = storeRepository.findById(storeId)
			.orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다."));

		DeliveryRegion deliveryRegion = DeliveryRegion.builder()
			.store(store)
			.addressName(request.addressName())
			.available(true) //처음 저장시 기본 true로 저장
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
			.orElseThrow(() -> new IllegalArgumentException("배달 지역을 찾을 수 없습니다"
				+ "."));

		deliveryRegion.changeAvailability(request.isAvailable());
	}

	@Override
	public List<DeliveryRegionResponseDto> getDeliveryRegions(Long storeId) {

		List<DeliveryRegion> regions = deliveryRegionRepository.findByStoreId(storeId);

		return regions.stream()
			.map(this::toResponse)
			.toList();
	}

	@Transactional(readOnly = true)
	public boolean isDeliverable(Long storeId, String address) {
		if (address == null || address.isBlank()) {
			return false;
		}
		List<DeliveryRegion> deliveryRegionList = deliveryRegionRepository.findByStoreIdAndAvailable(storeId, true);

		return deliveryRegionList.stream()
			.anyMatch(deliveryRegion -> address.contains(deliveryRegion.getAddressName()));
	}

	private DeliveryRegionResponseDto toResponse(DeliveryRegion region) {
		return DeliveryRegionResponseDto.builder()
			.deliveryRegionId(region.getId())
			.addressName(region.getAddressName())
			.isAvailable(region.isAvailable())
			.build();
	}
}