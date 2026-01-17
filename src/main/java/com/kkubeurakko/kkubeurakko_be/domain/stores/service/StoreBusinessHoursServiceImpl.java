package com.kkubeurakko.kkubeurakko_be.domain.stores.service;

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

	private void validateBreakTime(BusinessTimeDto dto) {
		if (dto.hasBreakTime()) {
			if (dto.breakStartTime() == null || dto.breakEndTime() == null) {
				throw new IllegalArgumentException("브레이크 시간이 설정되지 않았습니다.");
			}
		}
	}

	private StoreBusinessHours createEntity(
		Stores store,
		OperationType type,
		BusinessDay day,
		BusinessTimeDto dto
	) {
		validateBreakTime(dto);

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


}
