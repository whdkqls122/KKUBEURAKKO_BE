package com.kkubeurakko.kkubeurakko_be.domain.orders.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.kkubeurakko.kkubeurakko_be.domain.orders.config.DayRange;
import com.kkubeurakko.kkubeurakko_be.domain.orders.config.MonthRange;
import com.kkubeurakko.kkubeurakko_be.domain.orders.config.WeekRange;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.sales.SalesResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.orders.repository.StatisticsQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SalesServiceImpl implements SalesService {

	private final StatisticsQueryRepository statisticsQueryRepository;

	@Override
	public SalesResponseDto getDailySales(LocalDate date) {

		// 오늘
		LocalDateTime start = DayRange.DayStart(date);
		LocalDateTime end = DayRange.DayEnd(date);

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

		return buildSalesResponse(
			todaySales,
			yesterdaySales,
			todayOrders,
			yesterdayOrders,
			refundAmount,
			discountAmount
		);
	}

	@Override
	public SalesResponseDto getWeeklySales(LocalDate date) {
		// 이번 주
		LocalDateTime start = WeekRange.getWeekStart(date);
		LocalDateTime end = WeekRange.getWeekEnd(date);

		// 지난 주
		LocalDateTime prevStart = start.minusWeeks(1);
		LocalDateTime prevEnd = end.minusWeeks(1);

		// 매출
		long thisWeekSales = statisticsQueryRepository.getSalesAmount(start, end);
		long lastWeekSales = statisticsQueryRepository.getSalesAmount(prevStart, prevEnd);

		// 주문 수
		long thisWeekOrders = statisticsQueryRepository.getOrderCount(start, end);
		long lastWeekOrders = statisticsQueryRepository.getOrderCount(prevStart, prevEnd);

		// 환불 / 할인
		long refundAmount = statisticsQueryRepository.getRefundAmount(start, end);
		long discountAmount = statisticsQueryRepository.getDiscountAmount(start, end);

		return buildSalesResponse(
			thisWeekSales,
			lastWeekSales,
			thisWeekOrders,
			lastWeekOrders,
			refundAmount,
			discountAmount
		);
	}

	@Override
	public SalesResponseDto getMonthlySales(LocalDate date) {

		// 이번 달
		LocalDateTime start = MonthRange.getMonthStart(date);
		LocalDateTime end = MonthRange.getMonthEnd(date);

		// 지난 달
		LocalDateTime prevStart = MonthRange.getMonthStart(date.minusMonths(1));
		LocalDateTime prevEnd = MonthRange.getMonthEnd(date.minusMonths(1));

		// 매출
		long thisMonthSales = statisticsQueryRepository.getSalesAmount(start, end);
		long lastMonthSales = statisticsQueryRepository.getSalesAmount(prevStart, prevEnd);

		// 주문 수
		long thisMonthOrders = statisticsQueryRepository.getOrderCount(start, end);
		long lastMonthOrders = statisticsQueryRepository.getOrderCount(prevStart, prevEnd);

		// 환불 / 할인
		long refundAmount = statisticsQueryRepository.getRefundAmount(start, end);
		long discountAmount = statisticsQueryRepository.getDiscountAmount(start, end);

		return buildSalesResponse(
			thisMonthSales,
			lastMonthSales,
			thisMonthOrders,
			lastMonthOrders,
			refundAmount,
			discountAmount
		);
	}

	private SalesResponseDto buildSalesResponse(
		long currentSales,
		long prevSales,
		long currentOrders,
		long prevOrders,
		long refundAmount,
		long discountAmount
	) {
		return SalesResponseDto.builder()
			.salesAmount(currentSales)
			.salesDiff(currentSales - prevSales)

			.orderCount(currentOrders)
			.orderCountDiff(currentOrders - prevOrders)

			.refundAmount(refundAmount)
			.discountAmount(discountAmount)
			.build();
	}

}