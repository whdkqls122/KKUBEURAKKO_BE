package com.kkubeurakko.kkubeurakko_be.domain.orders.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

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

		return SalesResponseDto.builder()
			.salesAmount(todaySales)
			.salesDiff(todaySales - yesterdaySales)
			.orderCount(todayOrders)
			.orderCountDiff(todayOrders - yesterdayOrders)
			.refundAmount(refundAmount)
			.discountAmount(discountAmount)
			.build();
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
		Long thisWeekSales = statisticsQueryRepository.getSalesAmount(start, end);
		Long lastWeekSales = statisticsQueryRepository.getSalesAmount(prevStart, prevEnd);

		// 주문 수
		Long thisWeekOrders = statisticsQueryRepository.getOrderCount(start, end);
		Long lastWeekOrders = statisticsQueryRepository.getOrderCount(prevStart, prevEnd);

		// 환불 / 할인
		Long refundAmount = statisticsQueryRepository.getRefundAmount(start, end);
		Long discountAmount = statisticsQueryRepository.getDiscountAmount(start, end);

		return SalesResponseDto.builder()
			.salesAmount(thisWeekSales)
			.salesDiff(thisWeekSales - lastWeekSales)

			.orderCount(thisWeekOrders)
			.orderCountDiff(thisWeekOrders - lastWeekOrders)

			.refundAmount(refundAmount)
			.discountAmount(discountAmount)
			.build();
	}

	public SalesResponseDto getMonthlySales(LocalDate date) {

		// 이번 달
		LocalDateTime start = MonthRange.getMonthStart(date);
		LocalDateTime end = MonthRange.getMonthEnd(date);

		// 지난 달
		LocalDateTime prevStart = MonthRange.getMonthStart(date.minusMonths(1));
		LocalDateTime prevEnd = MonthRange.getMonthEnd(date.minusMonths(1));

		// 매출
		Long thisMonthSales = statisticsQueryRepository.getSalesAmount(start, end);
		Long lastMonthSales = statisticsQueryRepository.getSalesAmount(prevStart, prevEnd);

		// 주문 수
		Long thisMonthOrders = statisticsQueryRepository.getOrderCount(start, end);
		Long lastMonthOrders = statisticsQueryRepository.getOrderCount(prevStart, prevEnd);

		// 환불 / 할인
		Long refundAmount = statisticsQueryRepository.getRefundAmount(start, end);
		Long discountAmount = statisticsQueryRepository.getDiscountAmount(start, end);

		return SalesResponseDto.builder()
			.salesAmount(thisMonthSales)
			.salesDiff(thisMonthSales - lastMonthSales)

			.orderCount(thisMonthOrders)
			.orderCountDiff(thisMonthOrders - lastMonthOrders)

			.refundAmount(refundAmount)
			.discountAmount(discountAmount)
			.build();
	}
}