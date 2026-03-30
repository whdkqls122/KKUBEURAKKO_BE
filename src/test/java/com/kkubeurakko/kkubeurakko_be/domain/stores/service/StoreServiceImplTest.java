package com.kkubeurakko.kkubeurakko_be.domain.stores.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kkubeurakko.kkubeurakko_be.domain.stores.config.StoreOpenScheduler;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.StoreOperationInfoResponse;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.StoreUpdateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.BusinessDay;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.StoreBusinessHours;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.StoreStatus;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.StoreBusinessHoursRepository;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.StoreRepository;

@ExtendWith(MockitoExtension.class)
class StoreServiceImplTest {

	@Mock
	private StoreRepository storeRepository;

	@InjectMocks
	private StoreServiceImpl storeService;

	@Mock
	private StoreBusinessHoursRepository businessHoursRepository;

	@InjectMocks
	private StoreOpenScheduler scheduler;

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

	@Test
	@DisplayName("ALLDAY 영업시간이 존재하면 해당 시간으로 조회된다")
	void getOperationInfo_allDay() {

		Stores store = Stores.builder()
			.minOrderPrice(17000)
			.deliveryAvgTime(60)
			.packagingAvgTime(10)
			.build();

		StoreBusinessHours hours = StoreBusinessHours.builder()
			.openTime(LocalTime.of(9, 0))
			.closeTime(LocalTime.of(22, 0))
			.dayOfWeek(BusinessDay.ALLDAY)
			.store(store)
			.build();

		when(storeRepository.findById(1L))
			.thenReturn(Optional.of(store));

		when(businessHoursRepository.findByStoreIdAndDayOfWeek(1L, BusinessDay.ALLDAY))
			.thenReturn(Optional.of(hours));

		StoreOperationInfoResponse result =
			storeService.getOperationInfo(1L);

		assertThat(result.openTime()).isEqualTo(LocalTime.of(9, 0));
		assertThat(result.closeTime()).isEqualTo(LocalTime.of(22, 0));
		assertThat(result.minOrderPrice()).isEqualTo(17000);
	}

	@Test
	@DisplayName("다시 정상 영업이 실행되는지 테스트한다.")
	void reopenStores() {

		Stores store = Stores.builder()
			.status(StoreStatus.CLOSED)
			.pauseEndAt(LocalDateTime.now().minusMinutes(1))
			.build();

		when(storeRepository.findPausedStoresToReopen(StoreStatus.PAUSED, any()))
			.thenReturn(List.of(store));

		scheduler.reopenStores();

	}

}