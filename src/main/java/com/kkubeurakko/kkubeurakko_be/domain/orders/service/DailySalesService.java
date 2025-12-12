package com.kkubeurakko.kkubeurakko_be.domain.orders.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.sales.DailySalesResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.orders.repository.StatisticsQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DailySalesService {

	private final StatisticsQueryRepository statisticsQueryRepository;

	public DailySalesResponseDto getDailySales(LocalDate date) {

		LocalDateTime start = date.atStartOfDay();
		LocalDateTime end = date.atTime(23, 59, 59);

		LocalDateTime prevStart = date.minusDays(1).atStartOfDay();
		LocalDateTime prevEnd = date.minusDays(1).atTime(23, 59, 59);

		int todaySales = statisticsQueryRepository.getSalesAmount(start, end);
		int yesterdaySales = statisticsQueryRepository.getSalesAmount(prevStart, prevEnd);

		int todayOrders = statisticsQueryRepository.getOrderCount(start, end).intValue();
		int yesterdayOrders = statisticsQueryRepository.getOrderCount(prevStart, prevEnd).intValue();

		int refundAmount = statisticsQueryRepository.getRefundAmount(start, end);
		
		int discountAmount = statisticsQueryRepository.getDiscountAmount(start, end);

		return DailySalesResponseDto.builder()
			.salesAmount(todaySales)
			.salesDiff(todaySales - yesterdaySales)
			.orderCount(todayOrders)
			.orderCountDiff(todayOrders - yesterdayOrders)
			.refundAmount(refundAmount)
			.discountAmount(discountAmount)
			.build();
	}
}