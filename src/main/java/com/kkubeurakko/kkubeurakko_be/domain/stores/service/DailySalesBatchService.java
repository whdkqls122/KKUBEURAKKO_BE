package com.kkubeurakko.kkubeurakko_be.domain.stores.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.kkubeurakko.kkubeurakko_be.domain.orders.repository.StatisticsQueryRepository;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Sales;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.SalesRepository;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.StoreRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DailySalesBatchService {

	private final StatisticsQueryRepository statisticsQueryRepository;
	private final SalesRepository salesRepository;
	private final StoreRepository storeRepository;

	@Transactional
	public void saveDailySales(LocalDate targetDate) {

		// 이미 저장된 날짜면 스킵
		if (salesRepository.existsByDate(targetDate)) {
			return;
		}

		LocalDateTime start = targetDate.atStartOfDay();
		LocalDateTime end = targetDate.atTime(23, 59, 59);

		Long totalSales =
			statisticsQueryRepository.getSalesAmount(start, end);

		Long refundAmount =
			statisticsQueryRepository.getRefundAmount(start, end);

		// 단일 매장 가정
		Stores store = storeRepository.findById(1L)
			.orElseThrow(() -> new IllegalStateException("매장 없음"));

		Sales sales = Sales.builder()
			.date(targetDate)
			.totalPrice(totalSales)
			.refundPrice(refundAmount)
			.store(store)
			.build();

		salesRepository.save(sales);
	}
}
