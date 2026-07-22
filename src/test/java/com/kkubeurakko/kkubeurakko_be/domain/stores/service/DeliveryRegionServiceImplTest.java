package com.kkubeurakko.kkubeurakko_be.domain.stores.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.DeliveryRegionAbleResponse;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.DeliveryRegionRequest;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.DeliveryRegionResponse;
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
		Stores store = Stores.builder()
			.id(storeId)
			.build();

		DeliveryRegionRequest request = new DeliveryRegionRequest(3.0);

		when(storeRepository.findById(storeId))
			.thenReturn(Optional.of(store));

		// when
		deliveryRegionService.saveDeliveryRegion(storeId, request);

		// then
		ArgumentCaptor<DeliveryRegion> captor = ArgumentCaptor.forClass(DeliveryRegion.class);
		verify(deliveryRegionRepository).save(captor.capture());

		DeliveryRegion savedRegion = captor.getValue();
		assertThat(savedRegion.getStore()).isEqualTo(store);
		assertThat(savedRegion.getDistance()).isEqualTo(3.0);
	}

	@Test
	@DisplayName("배달 가능 지역 상태가 정상적으로 변경된다.")
	void updateDeliveryRegionStatus() {
		// given
		Long storeId = 1L;

		DeliveryRegion deliveryRegion = DeliveryRegion.builder()
			.distance(2.0)
			.build();

		DeliveryRegionRequest request = new DeliveryRegionRequest(5.0);

		when(deliveryRegionRepository.findByStoreId(storeId))
			.thenReturn(Optional.of(deliveryRegion));

		// when
		deliveryRegionService.updateDeliveryRegion(storeId, request);

		// then
		assertThat(deliveryRegion.getDistance()).isEqualTo(5.0);
		verify(deliveryRegionRepository, times(1)).findByStoreId(storeId);
		verify(deliveryRegionRepository, never()).save(any());
	}

	@Test
	@DisplayName("배달 반경과 가게 좌표가 정상 조회된다.")
	void getDeliveryRegion() {
		// given
		Long storeId = 1L;

		Stores store = Stores.builder()
			.id(storeId)
			.latitude(37.3940742)
			.longitude(126.9236632)
			.build();

		DeliveryRegion deliveryRegion = DeliveryRegion.builder()
			.distance(3.5)
			.store(store)
			.build();

		when(storeRepository.findById(storeId))
			.thenReturn(Optional.of(store));
		when(deliveryRegionRepository.findByStoreId(storeId))
			.thenReturn(Optional.of(deliveryRegion));

		// when
		DeliveryRegionResponse result = deliveryRegionService.getDeliveryRegion(storeId);

		// then
		assertThat(result.storeLat()).isEqualTo(37.3940742);
		assertThat(result.storeLng()).isEqualTo(126.9236632);
		assertThat(result.distance()).isEqualTo(3.5);
	}

	@Test
	@DisplayName("가게 반경 이내 좌표면 배달 가능하다.")
	void isDeliverable_true() {
		// given
		Long storeId = 1L;

		Stores store = Stores.builder()
			.id(storeId)
			.latitude(37.3940742)
			.longitude(126.9236632)
			.build();

		DeliveryRegion deliveryRegion = DeliveryRegion.builder()
			.distance(2.0) // 2km
			.build();

		when(storeRepository.findById(storeId))
			.thenReturn(Optional.of(store));
		when(deliveryRegionRepository.findByStoreId(storeId))
			.thenReturn(Optional.of(deliveryRegion));

		// 가게에서 매우 가까운 좌표
		double targetLat = 37.3950000;
		double targetLng = 126.9240000;

		// when
		DeliveryRegionAbleResponse result =
			deliveryRegionService.isDeliverable(storeId, targetLat, targetLng);

		// then
		assertThat(result.able()).isTrue();
	}

	@Test
	@DisplayName("가게 반경 밖 좌표면 배달 불가능하다.")
	void isDeliverable_false() {
		// given
		Long storeId = 1L;

		Stores store = Stores.builder()
			.id(storeId)
			.latitude(37.3940742)
			.longitude(126.9236632)
			.build();

		DeliveryRegion deliveryRegion = DeliveryRegion.builder()
			.distance(1.0) // 1km
			.build();

		when(storeRepository.findById(storeId))
			.thenReturn(Optional.of(store));
		when(deliveryRegionRepository.findByStoreId(storeId))
			.thenReturn(Optional.of(deliveryRegion));

		// 1km보다 충분히 먼 좌표
		double targetLat = 37.4100000;
		double targetLng = 126.9400000;

		// when
		DeliveryRegionAbleResponse result =
			deliveryRegionService.isDeliverable(storeId, targetLat, targetLng);

		// then
		assertThat(result.able()).isFalse();
	}
}