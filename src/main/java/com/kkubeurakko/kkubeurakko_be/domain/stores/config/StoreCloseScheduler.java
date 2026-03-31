package com.kkubeurakko.kkubeurakko_be.domain.stores.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.StoreBusinessHours;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.StoreStatus;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.StoreRepository;
import com.kkubeurakko.kkubeurakko_be.domain.stores.service.StoreBusinessHoursService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StoreCloseScheduler {

	private final StoreRepository storeRepository;
	private final StoreBusinessHoursService storeBusinessHoursService;

	@Scheduled(fixedDelay = 60_000) // 1분마다
	@Transactional
	public void closeStores() {

		List<Stores> stores =
			storeRepository.findStoresByStatuses(
				List.of(StoreStatus.OPEN, StoreStatus.PAUSED)
			);

		LocalDateTime now = LocalDateTime.now();

		for (Stores store : stores) {

			if (shouldClose(store, now)) {
				store.close();
			}
		}
	}

	private boolean shouldClose(Stores store, LocalDateTime now) {

		LocalDate businessDate =
			storeBusinessHoursService.resolveBusinessDate(store.getId());
		StoreBusinessHours hours =
			storeBusinessHoursService.getHoursByDate(store.getId(), businessDate);

		LocalDateTime closeDateTime;

		if (hours.getCloseTime().isBefore(hours.getOpenTime())) {
			closeDateTime =
				LocalDateTime.of(businessDate.plusDays(1), hours.getCloseTime());
		} else {
			closeDateTime =
				LocalDateTime.of(businessDate, hours.getCloseTime());
		}

		return now.isAfter(closeDateTime);
	}
}