package com.kkubeurakko.kkubeurakko_be.domain.stores.config;

import java.time.LocalDate;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kkubeurakko.kkubeurakko_be.domain.stores.service.DailySalesBatchService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DailySalesScheduler {

	private final DailySalesBatchService dailySalesBatchService;

	//새벽 1시
	@Scheduled(cron = "0 0 1 * * *")
	public void saveYesterdaySales() {

		LocalDate yesterday = LocalDate.now().minusDays(1);
		dailySalesBatchService.saveDailySales(yesterday);
	}
}