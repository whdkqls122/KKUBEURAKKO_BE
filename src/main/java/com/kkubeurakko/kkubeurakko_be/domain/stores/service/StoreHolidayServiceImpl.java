package com.kkubeurakko.kkubeurakko_be.domain.stores.service;

import java.time.DayOfWeek;
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

		boolean exists = holidayRepository.existsByStoreIdAndHolidayTypeAndDayOfWeek(
			storeId,
			HolidayType.WEEKLY,
			request.dayOfWeek()
		);

		if (exists) {
			throw new IllegalStateException("이미 해당 요일에 반복 휴무가 존재합니다.");
		}

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

	@Override
	public void deleteOnceHoliday(Long storeId, LocalDate holidayDate) {

		boolean exists = holidayRepository.existsByStoreIdAndHolidayTypeAndHolidayDate(
			storeId,
			HolidayType.ONCE,
			holidayDate
		);

		if (!exists) {
			throw new IllegalArgumentException("해당 날짜의 휴무일이 존재하지 않습니다.");
		}

		holidayRepository.deleteByStoreIdAndHolidayTypeAndHolidayDate(
			storeId,
			HolidayType.ONCE,
			holidayDate
		);
	}

	@Override
	public void deleteWeeklyHoliday(Long storeId, DayOfWeek dayOfWeek) {

		boolean exists = holidayRepository.existsByStoreIdAndHolidayTypeAndDayOfWeek(
			storeId,
			HolidayType.WEEKLY,
			dayOfWeek
		);

		if (!exists) {
			throw new IllegalArgumentException("해당 요일의 반복 휴무가 존재하지 않습니다.");
		}

		holidayRepository.deleteByStoreIdAndHolidayTypeAndDayOfWeek(
			storeId,
			HolidayType.WEEKLY,
			dayOfWeek
		);
	}

	private Stores findStore(Long storeId) {
		return storeRepository.findById(storeId)
			.orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다."));
	}

}
