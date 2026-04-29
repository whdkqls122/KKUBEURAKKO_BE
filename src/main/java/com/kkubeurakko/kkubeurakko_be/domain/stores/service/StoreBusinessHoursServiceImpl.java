package com.kkubeurakko.kkubeurakko_be.domain.stores.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.BusinessTimeDto;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.StoreBusinessHoursRequest;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.StoreBusinessHoursResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.BusinessDay;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.OperationType;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.StoreBusinessHours;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.StoreBusinessHoursRepository;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.StoreRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreBusinessHoursServiceImpl implements StoreBusinessHoursService {

	private final StoreBusinessHoursRepository businessHoursRepository;
	private final StoreRepository storeRepository;

	@Override
	@Transactional
	public void saveBusinessHours(Long storeId, StoreBusinessHoursRequest request) {

		Stores store = storeRepository.findById(storeId)
			.orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다."));

		businessHoursRepository.deleteByStoreId(storeId);

		if (request.operationType() == OperationType.SAME) {
			saveSameTime(store, request.commonTime().get(0));
		} else {
			saveWeeklyTime(store, request.commonTime());
		}
	}

	@Override
	public List<StoreBusinessHoursResponseDto> getBusinessHours(Long storeId) {

		List<StoreBusinessHours> hours =
			businessHoursRepository.findByStoreId(storeId);

		if (hours.isEmpty()) {
			return List.of();
		}

		OperationType type = hours.get(0).getOperationType();

		if (type == OperationType.SAME) {
			return List.of(toResponse(hours.get(0)));
		}

		return hours.stream()
			.map(this::toResponse)
			.toList();
	}

	//영업 시간이 대부분 익일 새벽에 끝나는 이유로 해당 부분 계산
	@Override
	public LocalDate resolveBusinessDate(Long storeId) {

		LocalDateTime now = LocalDateTime.now();
		LocalDate today = now.toLocalDate();
		LocalDate yesterday = today.minusDays(1);

		// 오늘 영업시간 조회
		StoreBusinessHours todayHours = findHours(storeId, today);
		LocalDateTime todayOpen = LocalDateTime.of(today, todayHours.getOpenTime());
		LocalDateTime todayClose = buildCloseDateTime(todayHours, today);

		// 아직 오늘 영업이 끝나지 않았다면 → 오늘 영업일
		if (!now.isBefore(todayOpen) && now.isBefore(todayClose)) {
			return today;
		}

		// 어제 영업시간
		StoreBusinessHours yesterdayHours = findHours(storeId, yesterday);
		LocalDateTime yesterdayOpen =
			LocalDateTime.of(yesterday, yesterdayHours.getOpenTime());
		LocalDateTime yesterdayClose =
			buildCloseDateTime(yesterdayHours, yesterday);

		// 어제 영업이 아직 끝나지 않았으면 → 어제 영업일
		if (!now.isBefore(yesterdayOpen) && now.isBefore(yesterdayClose)) {
			return yesterday;
		}

		log.info("RESOLVE → now={}, todayOpen={}, todayClose={}, yesterdayOpen={}, yesterdayClose={}",
			now, todayOpen, todayClose, yesterdayOpen, yesterdayClose);
		// 그 외 → 오늘 영업일
		return today;
	}

	private void saveSameTime(Stores store, BusinessTimeDto dto) {
		for (BusinessDay day : BusinessDay.values()) {
			if (day == BusinessDay.ALLDAY)
				continue;

			businessHoursRepository.save(
				createEntity(store, OperationType.SAME, day, dto)
			);
		}
	}

	private void saveWeeklyTime(Stores store, List<BusinessTimeDto> list) {
		for (BusinessTimeDto dto : list) {
			businessHoursRepository.save(
				createEntity(store, OperationType.WEEKLY, dto.dayOfWeek(), dto)
			);
		}
	}

	private StoreBusinessHours createEntity(
		Stores store,
		OperationType type,
		BusinessDay day,
		BusinessTimeDto dto
	) {

		return StoreBusinessHours.builder()
			.store(store)
			.operationType(type)
			.dayOfWeek(day)
			.openTime(dto.openTime())
			.closeTime(dto.closeTime())
			.hasBreakTime(dto.hasBreakTime())
			.breakStartTime(dto.breakStartTime())
			.breakEndTime(dto.breakEndTime())
			.build();
	}

	private StoreBusinessHoursResponseDto toResponse(StoreBusinessHours entity) {
		return StoreBusinessHoursResponseDto.builder()
			.operationType(entity.getOperationType())
			.dayOfWeek(entity.getDayOfWeek())
			.openTime(entity.getOpenTime())
			.closeTime(entity.getCloseTime())
			.hasBreakTime(entity.isHasBreakTime())
			.breakStartTime(entity.getBreakStartTime())
			.breakEndTime(entity.getBreakEndTime())
			.build();
	}

	private StoreBusinessHours findHours(Long storeId, LocalDate date) {

		BusinessDay day = BusinessDay.from(date.getDayOfWeek());
		List<StoreBusinessHours> hours =
			businessHoursRepository.findByStoreId(storeId);

		if (hours.isEmpty()) {
			throw new IllegalStateException("영업 시간이 설정되지 않았습니다.");
		}
		if (hours.get(0).getOperationType() == OperationType.SAME) {
			return hours.get(0);
		}

		return hours.stream()
			.filter(h -> h.getDayOfWeek() == day)
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("해당 요일 영업 시간이 없습니다."));
	}

	private LocalDateTime buildCloseDateTime(
		StoreBusinessHours h,
		LocalDate baseDate
	) {
		// 새벽 마감 (open > close) 고려
		if (h.getCloseTime().isBefore(h.getOpenTime())) {
			return LocalDateTime.of(baseDate.plusDays(1), h.getCloseTime());
		}
		return LocalDateTime.of(baseDate, h.getCloseTime());
	}

	public StoreBusinessHours getHoursByDate(Long storeId, LocalDate date) {

		BusinessDay day = BusinessDay.from(date.getDayOfWeek());

		List<StoreBusinessHours> hours =
			businessHoursRepository.findByStoreId(storeId);

		if (hours.isEmpty()) {
			throw new IllegalStateException("영업시간 없음");
		}

		if (hours.get(0).getOperationType() == OperationType.SAME) {
			return hours.get(0);
		}

		return hours.stream()
			.filter(h -> h.getDayOfWeek() == day)
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("요일 없음"));
	}

}
