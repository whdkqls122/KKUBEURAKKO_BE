package com.kkubeurakko.kkubeurakko_be.domain.stores.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.kkubeurakko.kkubeurakko_be.domain.orders.repository.StatisticsQueryRepository;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Sales;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.SalesRepository;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.StoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DailySalesBatchService {

	private final StatisticsQueryRepository statisticsQueryRepository;
	private final SalesRepository salesRepository;
	private final StoreRepository storeRepository;

	public void saveDailySales(LocalDate targetDate) {

		// 이미 저장된 날짜면 스킵
		if (salesRepository.existsByDate(targetDate)) {
			return;
		}

		LocalDateTime start = targetDate.atStartOfDay();
		LocalDateTime end = targetDate.atTime(23, 59, 59);

		Long totalSalesLong =
			statisticsQueryRepository.getSalesAmount(start, end);

		Long refundAmountLong =
			statisticsQueryRepository.getRefundAmount(start, end);

		Stores store = storeRepository.findById(1L)
			.orElseThrow(() -> new IllegalStateException("매장 없음"));

		int totalSales = totalSalesLong != null ? totalSalesLong.intValue() : 0;
		int refundAmount = refundAmountLong != null ? refundAmountLong.intValue() : 0;

		Sales sales = Sales.builder()
			.date(targetDate)
			.totalPrice(totalSales)
			.refundPrice(refundAmount)
			.store(store)
			.build();

		salesRepository.save(sales);
	}
}
