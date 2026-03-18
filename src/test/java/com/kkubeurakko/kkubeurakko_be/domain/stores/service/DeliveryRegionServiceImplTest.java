package com.kkubeurakko.kkubeurakko_be.domain.stores.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.DeliveryRegionCreateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.DeliveryRegionResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.DeliveryRegionStatusUpdateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.DeliveryRegion;
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

	@Test
	@DisplayName("배달 가능 지역 상태가 정상적으로 변경된다.")
	void updateDeliveryRegionStatus() {
		// given
		Long storeId = 1L;

		DeliveryRegion deliveryRegion = DeliveryRegion.builder()
			.available(true)
			.build();

		when(deliveryRegionRepository.findById(1L))
			.thenReturn(Optional.of(deliveryRegion));

		DeliveryRegionStatusUpdateRequest request =
			new DeliveryRegionStatusUpdateRequest(1L, false);

		// when
		deliveryRegionService.updateDeliveryRegionStatus(storeId, request);

		// then
		assertFalse(deliveryRegion.isAvailable());
		verify(deliveryRegionRepository, times(1)).findById(1L);
		verify(deliveryRegionRepository, never()).save(any());
	}

	@Test
	@DisplayName("배달 가능 지역 리스트가 정상적으로 조회된다")
	void getDeliveryRegions() {
		Stores store = Stores.builder().id(1L).build();

		DeliveryRegion region = DeliveryRegion.builder()
			.id(1L)
			.addressName("경기 안양시 동안구 비산2동")
			.available(true)
			.store(store)
			.build();

		when(deliveryRegionRepository.findByStoreId(1L))
			.thenReturn(List.of(region));

		List<DeliveryRegionResponseDto> result =
			deliveryRegionService.getDeliveryRegions(1L);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).deliveryRegionId()).isEqualTo(1L);
		assertThat(result.get(0).addressName()).isEqualTo("경기 안양시 동안구 비산2동");
		assertThat(result.get(0).isAvailable()).isTrue();

		verify(deliveryRegionRepository, times(1)).findByStoreId(1L);
	}
}