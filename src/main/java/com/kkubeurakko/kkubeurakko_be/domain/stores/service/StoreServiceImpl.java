package com.kkubeurakko.kkubeurakko_be.domain.stores.service;

import org.springframework.stereotype.Service;

import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.StoreUpdateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.StoreRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

	private final StoreRepository storeRepository;

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
}