package com.kkubeurakko.kkubeurakko_be.domain.stores.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.OnceHolidayCreateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.StoreHolidayResponse;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.WeeklyHolidayCreateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.HolidayType;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.StoreHoliday;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.StoreHolidayRepository;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.StoreRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreHolidayServiceImpl implements StoreHolidayService {

	private final StoreRepository storeRepository;
	private final StoreHolidayRepository holidayRepository;

	@Override
	public void createOnceHoliday(Long storeId, OnceHolidayCreateRequest request) {
		Stores store = findStore(storeId);

		StoreHoliday holiday = StoreHoliday.builder()
			.store(store)
			.holidayType(HolidayType.ONCE)
			.holidayDate(request.holidayDate())
			.build();

		holidayRepository.save(holiday);
	}

	@Override
	public void createWeeklyHoliday(Long storeId, WeeklyHolidayCreateRequest request) {
		Stores store = findStore(storeId);

		StoreHoliday holiday = StoreHoliday.builder()
			.store(store)
			.holidayType(HolidayType.WEEKLY)
			.dayOfWeek(request.dayOfWeek())
			.build();

		holidayRepository.save(holiday);
	}

	@Override
	public boolean isHoliday(Long storeId, LocalDate date) {
		return holidayRepository.findByStoreId(storeId).stream()
			.anyMatch(h ->
				(h.getHolidayType() == HolidayType.ONCE &&
					date.equals(h.getHolidayDate())) ||
					(h.getHolidayType() == HolidayType.WEEKLY &&
						date.getDayOfWeek() == h.getDayOfWeek())
			);
	}

	@Override
	public List<StoreHolidayResponse> getHolidays(Long storeId) {

		return holidayRepository.findByStoreId(storeId)
			.stream()
			.map(h -> StoreHolidayResponse.builder()
				.holidayType(h.getHolidayType())
				.holidayDate(h.getHolidayDate())
				.dayOfWeek(h.getDayOfWeek())
				.build()
			)
			.toList();
	}

	private Stores findStore(Long storeId) {
		return storeRepository.findById(storeId)
			.orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다."));
	}

}
