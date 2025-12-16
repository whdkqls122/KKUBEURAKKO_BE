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

		// 오늘
		LocalDateTime start = date.atStartOfDay();
		LocalDateTime end = date.atTime(23, 59, 59);

		// 어제
		LocalDateTime prevStart = date.minusDays(1).atStartOfDay();
		LocalDateTime prevEnd = date.minusDays(1).atTime(23, 59, 59);

		// 매출
		long todaySales = statisticsQueryRepository.getSalesAmount(start, end);
		long yesterdaySales = statisticsQueryRepository.getSalesAmount(prevStart, prevEnd);

		// 주문 수
		long todayOrders = statisticsQueryRepository.getOrderCount(start, end);
		long yesterdayOrders = statisticsQueryRepository.getOrderCount(prevStart, prevEnd);

		// 환불 / 할인
		long refundAmount = statisticsQueryRepository.getRefundAmount(start, end);
		long discountAmount = statisticsQueryRepository.getDiscountAmount(start, end);

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