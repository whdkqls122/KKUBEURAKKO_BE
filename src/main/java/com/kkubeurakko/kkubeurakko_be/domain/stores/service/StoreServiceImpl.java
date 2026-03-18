package com.kkubeurakko.kkubeurakko_be.domain.stores.service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.StoreOperationCreateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.StoreOperationInfoResponse;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.StorePauseRequest;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.StoreUpdateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.BusinessDay;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.StoreBusinessHours;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.StoreBusinessHoursRepository;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.StoreRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

	private final StoreRepository storeRepository;
	private final StoreBusinessHoursRepository businessHoursRepository;

	@Override
	@Transactional
	public void createOperationInfo(Long storeId, StoreOperationCreateRequest request) {

		Stores store = storeRepository.findById(storeId)
			.orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다."));

		store.updateBasicInfo(
			request.minOrderPrice(),
			request.deliveryAvgTime(),
			request.packagingAvgTime()
		);
	}

	@Override
	@Transactional
	public void updateStoreBasicInfo(Long storeId, StoreUpdateRequest request) {
		Stores store = storeRepository.findById(storeId)
			.orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다."));

		store.updateBasicInfo(
			request.minOrderPrice(),
			request.deliveryAvgTime(),
			request.packagingAvgTime()
		);
	}

	public StoreOperationInfoResponse getOperationInfo(Long storeId) {

		Stores store = storeRepository.findById(storeId)
			.orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다."));

		Optional<StoreBusinessHours> allDay =
			businessHoursRepository.findByStoreIdAndDayOfWeek(storeId, BusinessDay.ALLDAY);

		StoreBusinessHours businessHours;

		if (allDay.isPresent()) {
			businessHours = allDay.get();
		} else {
			BusinessDay today = BusinessDay.from(LocalDate.now().getDayOfWeek());

			businessHours = businessHoursRepository
				.findByStoreIdAndDayOfWeek(storeId, today)
				.orElseThrow(() -> new IllegalArgumentException("오늘의 영업시간이 없습니다."));
		}

		return toResponse(store, businessHours);
	}

	@Override
	@Transactional
	public void pauseStore(Long storeId, StorePauseRequest request) {

		Stores store = storeRepository.findById(storeId)
			.orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다."));

		store.pause(
			request.startDateTime(),
			request.endDateTime()
		);
	}

	private StoreOperationInfoResponse toResponse(
		Stores store,
		StoreBusinessHours businessHours
	) {
		return StoreOperationInfoResponse.builder()
			.isOpen(store.isOpen())
			.openTime(businessHours.getOpenTime())
			.closeTime(businessHours.getCloseTime())
			.minOrderPrice(store.getMinOrderPrice())
			.deliveryAvgTime(store.getDeliveryAvgTime())
			.packagingAvgTime(store.getPackagingAvgTime())
			.build();
	}
}