package com.kkubeurakko.kkubeurakko_be.domain.stores.service;

import static org.mockito.Mockito.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.OnceHolidayCreateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.WeeklyHolidayCreateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.HolidayType;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.StoreHoliday;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.StoreHolidayRepository;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.StoreRepository;

@ExtendWith(MockitoExtension.class)
class StoreHolidayServiceImplTest {

	@Mock
	private StoreRepository storeRepository;

	@Mock
	private StoreHolidayRepository holidayRepository;

	@InjectMocks
	private StoreHolidayServiceImpl service;

	@Test
	void createOnceHoliday() {
		Stores store = Stores.builder().build();
		when(storeRepository.findById(1L)).thenReturn(Optional.of(store));

		service.createOnceHoliday(
			1L,
			new OnceHolidayCreateRequest(HolidayType.ONCE, LocalDate.now())
		);

		verify(holidayRepository).save(any(StoreHoliday.class));
	}

	@Test
	void createWeeklyHoliday() {
		Stores store = Stores.builder().build();
		when(storeRepository.findById(1L)).thenReturn(Optional.of(store));

		service.createWeeklyHoliday(
			1L,
			new WeeklyHolidayCreateRequest(HolidayType.WEEKLY, DayOfWeek.TUESDAY)
		);

		verify(holidayRepository).save(any(StoreHoliday.class));
	}
}
