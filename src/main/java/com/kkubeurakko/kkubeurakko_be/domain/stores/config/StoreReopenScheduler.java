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
public class StoreReopenScheduler {

	private final StoreRepository storeRepository;
	private final StoreBusinessHoursService businessHoursService;

	@Scheduled(fixedRate = 10_000)
	@Transactional
	public void reopenStores() {

		LocalDateTime now = LocalDateTime.now();
		List<Stores> stores = storeRepository.findClosedStores(StoreStatus.CLOSED);

		for (Stores store : stores) {

			StoreBusinessHours hours =
				businessHoursService.getHoursByDate(
					store.getId(),
					now.toLocalDate()
				);

			LocalDateTime openTime =
				LocalDateTime.of(now.toLocalDate(), hours.getOpenTime());

			LocalDateTime closeTime =
				buildCloseDateTime(hours, now.toLocalDate());

			if (!now.isBefore(openTime) && now.isBefore(closeTime)) {
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