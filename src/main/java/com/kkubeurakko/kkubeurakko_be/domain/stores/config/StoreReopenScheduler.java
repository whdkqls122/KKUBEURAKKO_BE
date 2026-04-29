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
public class StoreReopenScheduler {

	private final StoreRepository storeRepository;
	private final StoreBusinessHoursService businessHoursService;
	private final StoreHolidayService storeHolidayService;

	@Scheduled(fixedRate = 10_000)
	@Transactional
	public void reopenStores() {

		LocalDateTime now = LocalDateTime.now();
		List<Stores> stores = storeRepository.findClosedStores(StoreStatus.CLOSED);

		for (Stores store : stores) {

			LocalDate businessDate =
				businessHoursService.resolveBusinessDate(store.getId());

			if (storeHolidayService.isHoliday(store.getId(), businessDate)) {
				continue;
			}

			StoreBusinessHours hours =
				businessHoursService.getHoursByDate(
					store.getId(),
					businessDate
				);

			LocalDateTime openTime =
				LocalDateTime.of(businessDate, hours.getOpenTime());

			LocalDateTime closeTime =
				buildCloseDateTime(hours, businessDate);

			if (store.getStatus() == StoreStatus.CLOSED &&
				!now.isBefore(openTime) &&
				now.isBefore(closeTime)) {

				// log.info("REOPEN → storeId={}, now={}, businessDate={}, openTime={}, closeTime={}",
				// 	store.getId(), now, businessDate, openTime, closeTime);
				store.reopen();
			}

		}
	}

	private LocalDateTime buildCloseDateTime(
		StoreBusinessHours h,
		LocalDate baseDate
	) {
		if (h.getCloseTime().isBefore(h.getOpenTime())) {
			return LocalDateTime.of(baseDate.plusDays(1), h.getCloseTime());
		}
		return LocalDateTime.of(baseDate, h.getCloseTime());
	}
}