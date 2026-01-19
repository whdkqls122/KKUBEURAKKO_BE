package com.kkubeurakko.kkubeurakko_be.domain.stores.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.StoreHolidayCreateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.StoreHolidayResponse;
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
	public void createHoliday(Long storeId, StoreHolidayCreateRequest request) {
		Stores store = storeRepository.findById(storeId)
			.orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다."));

		validate(request);

		StoreHoliday holiday = StoreHoliday.builder()
			.store(store)
			.holidayType(request.holidayType())
			.holidayDate(request.holidayDate())
			.dayOfWeek(request.dayOfWeek())
			.build();

		holidayRepository.save(holiday);
	}

	@Override
	public List<StoreHolidayResponse> getHolidays(Long storeId) {
		return holidayRepository.findByStoreId(storeId).stream()
			.map(this::toResponse)
			.toList();
	}

	@Override
	public boolean isHoliday(Long storeId, LocalDate date) {
		return holidayRepository.findByStoreId(storeId).stream()
			.anyMatch(holiday ->
				(holiday.getHolidayType() == HolidayType.ONCE &&
					date.equals(holiday.getHolidayDate())) ||
					(holiday.getHolidayType() == HolidayType.WEEKLY &&
						date.getDayOfWeek() == holiday.getDayOfWeek())
			);
	}

	private void validate(StoreHolidayCreateRequest request) {
		if (request.holidayType() == HolidayType.ONCE && request.holidayDate() == null) {
			throw new IllegalArgumentException("단일 휴무는 날짜가 필요합니다.");
		}
		if (request.holidayType() == HolidayType.WEEKLY && request.dayOfWeek() == null) {
			throw new IllegalArgumentException("반복 휴무는 요일이 필요합니다.");
		}
	}

	private StoreHolidayResponse toResponse(StoreHoliday entity) {
		return StoreHolidayResponse.builder()
			.holidayType(entity.getHolidayType())
			.holidayDate(entity.getHolidayDate())
			.dayOfWeek(entity.getDayOfWeek())
			.build();
	}
}
