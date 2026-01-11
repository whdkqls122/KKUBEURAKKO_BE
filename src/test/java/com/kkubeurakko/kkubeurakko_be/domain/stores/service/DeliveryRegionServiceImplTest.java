package com.kkubeurakko.kkubeurakko_be.domain.stores.service;

import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.DeliveryRegionCreateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.DeliveryRegionRepository;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.StoreRepository;

@ExtendWith(MockitoExtension.class)
class DeliveryRegionServiceImplTest {

	@Mock
	private DeliveryRegionRepository deliveryRegionRepository;

	@Mock
	private StoreRepository storeRepository;

	@InjectMocks
	private DeliveryRegionServiceImpl deliveryRegionService;

	@Test
	@DisplayName("배달 가능 지역이 기본 true 상태로 정상 저장된다.")
	void saveDeliveryRegion() {
		// given
		Long storeId = 1L;
		Stores store = Stores.builder().build();

		when(storeRepository.findById(storeId))
			.thenReturn(Optional.of(store));

		DeliveryRegionCreateRequest request =
			new DeliveryRegionCreateRequest("경기 안양시 동안구 비산2동");

		// when
		deliveryRegionService.saveDeliveryRegion(storeId, request);

		// then
		verify(deliveryRegionRepository, times(1))
			.save(argThat(deliveryRegion ->
				deliveryRegion.getStore() == store &&
					deliveryRegion.getAddressName().equals("경기 안양시 동안구 비산2동") &&
					deliveryRegion.isAvailable()
			));
	}
}