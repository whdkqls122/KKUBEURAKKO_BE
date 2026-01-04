package com.kkubeurakko.kkubeurakko_be.domain.stores.service;

import static org.mockito.Mockito.*;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.BusinessTimeDto;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.StoreBusinessHoursRequest;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.BusinessDay;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.OperationType;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.StoreBusinessHoursRepository;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.StoreRepository;

@ExtendWith(MockitoExtension.class)
class StoreBusinessHoursServiceImplTest {

	@Mock
	private StoreBusinessHoursRepository businessHoursRepository;

	@Mock
	private StoreRepository storeRepository;

	@InjectMocks
	private StoreBusinessHoursServiceImpl storeBusinessHoursService;

	@Test
	@DisplayName("영업시간이 정상적으로 저장 되는지 검증한다.")
	void saveBusinessHours() {
		Stores store = Stores.builder().build();

		when(storeRepository.findById(1L))
			.thenReturn(Optional.of(store));

		StoreBusinessHoursRequest request =
			new StoreBusinessHoursRequest(
				OperationType.SAME,
				List.of(
					new BusinessTimeDto(
						BusinessDay.ALLDAY,
						LocalTime.of(9, 0),
						LocalTime.of(22, 0),
						true,
						LocalTime.of(15, 0),
						LocalTime.of(16, 0)
					)
				)
			);

		storeBusinessHoursService.saveBusinessHours(1L, request);

		verify(businessHoursRepository, times(7)).save(any());
	}
}
