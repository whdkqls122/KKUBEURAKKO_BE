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
import com.kkubeurakko.kkubeurakko_be.domain.stores.service.StoreHolidayService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoreCloseScheduler {

	private final StoreRepository storeRepository;
	private final StoreBusinessHoursService storeBusinessHoursService;
	private final StoreHolidayService storeHolidayService;

	@Scheduled(fixedDelay = 60_000) // 1분마다
	@Transactional
	public void closeStores() {

		List<Stores> stores =
			storeRepository.findStoresByStatuses(
				List.of(StoreStatus.OPEN, StoreStatus.PAUSED)
			);

		LocalDateTime now = LocalDateTime.now();
		LocalDate today = now.toLocalDate();

		for (Stores store : stores) {

			LocalDate businessDate =
				storeBusinessHoursService.resolveBusinessDate(store.getId());

			if (storeHolidayService.isHoliday(store.getId(), businessDate)) {
				if (store.getStatus() != StoreStatus.CLOSED) {
					store.close();
				}
				continue;
			}

			if (shouldClose(store, now, businessDate)) {
				if (store.getStatus() == StoreStatus.OPEN
					|| store.getStatus() == StoreStatus.PAUSED) {

					store.close();
				}
			}
		}
	}

	private boolean shouldClose(
		Stores store,
		LocalDateTime now,
		LocalDate businessDate
	) {

		StoreBusinessHours hours =
			storeBusinessHoursService.getHoursByDate(store.getId(), businessDate);

		LocalDateTime openDateTime =
			LocalDateTime.of(businessDate, hours.getOpenTime());

		LocalDateTime closeDateTime;

		if (hours.getCloseTime().isBefore(hours.getOpenTime())) {
			closeDateTime =
				LocalDateTime.of(businessDate.plusDays(1), hours.getCloseTime());
		} else {
			closeDateTime =
				LocalDateTime.of(businessDate, hours.getCloseTime());
		}

		// log.info("CLOSE CHECK → storeId={}, now={}, businessDate={}, closeTime={}, status={}",
		// 	store.getId(), now, businessDate, closeDateTime, store.getStatus());

		return now.isBefore(openDateTime) || !now.isBefore(closeDateTime);
	}
}