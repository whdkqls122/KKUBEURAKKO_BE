package com.kkubeurakko.kkubeurakko_be.domain.stores.config;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.StoreRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StoreOpenScheduler {

	private final StoreRepository storeRepository;

	@Scheduled(fixedDelay = 60_000) // 1분마다
	@Transactional
	public void reopenStores() {

		List<Stores> stores =
			storeRepository.findPausedStoresToReopen(LocalDateTime.now());

		for (Stores store : stores) {
			store.reopen();
		}
	}
}