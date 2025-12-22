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

		PeriodSalesData today = getPeriodSalesData(start, end);
		PeriodSalesData yesterday = getPeriodSalesData(prevStart, prevEnd);

		return buildSalesResponse(
			today.sales,
			yesterday.sales,
			today.orders,
			yesterday.orders,
			today.refund,
			today.discount
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

		PeriodSalesData thisWeekOrders = getPeriodSalesData(start, end);
		PeriodSalesData lastWeekOrders = getPeriodSalesData(prevStart, prevEnd);

		return buildSalesResponse(
			thisWeekOrders.sales,
			lastWeekOrders.sales,
			thisWeekOrders.orders,
			lastWeekOrders.orders,
			thisWeekOrders.refund,
			thisWeekOrders.discount
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

		PeriodSalesData thisMonthOrders = getPeriodSalesData(start, end);
		PeriodSalesData lastMonthOrders = getPeriodSalesData(prevStart, prevEnd);

		return buildSalesResponse(
			thisMonthOrders.sales,
			lastMonthOrders.sales,
			thisMonthOrders.orders,
			lastMonthOrders.orders,
			thisMonthOrders.refund,
			thisMonthOrders.discount
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

	private static class PeriodSalesData {
		long sales;
		long orders;
		long refund;
		long discount;

		PeriodSalesData(long sales, long orders, long refund, long discount) {
			this.sales = sales;
			this.orders = orders;
			this.refund = refund;
			this.discount = discount;
		}
	}

	private PeriodSalesData getPeriodSalesData(LocalDateTime start, LocalDateTime end) {
		long sales = statisticsQueryRepository.getSalesAmount(start, end);
		long orders = statisticsQueryRepository.getOrderCount(start, end);
		long refund = statisticsQueryRepository.getRefundAmount(start, end);
		long discount = statisticsQueryRepository.getDiscountAmount(start, end);

		return new PeriodSalesData(sales, orders, refund, discount);
	}

}