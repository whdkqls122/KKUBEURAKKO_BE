package com.kkubeurakko.kkubeurakko_be.domain.stores.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.StoreUpdateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.StoreRepository;

@ExtendWith(MockitoExtension.class)
class StoreServiceImplTest {

	@Mock
	private StoreRepository storeRepository;

	@InjectMocks
	private StoreServiceImpl storeService;

	@Test
	void storeUpdate() {
		// given
		Stores store = Stores.builder()
			.minOrderPrice(15000)
			.deliveryAvgTime(50)
			.packagingAvgTime(5)
			.build();

		when(storeRepository.findById(1L))
			.thenReturn(Optional.of(store));

		StoreUpdateRequest request = new StoreUpdateRequest(
			17000,
			60,
			10
		);

		// when
		storeService.updateStoreBasicInfo(1L, request);

		// then
		assertThat(store.getMinOrderPrice()).isEqualTo(17000);
		assertThat(store.getDeliveryAvgTime()).isEqualTo(60);
		assertThat(store.getPackagingAvgTime()).isEqualTo(10);
	}
}