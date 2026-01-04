package com.kkubeurakko.kkubeurakko_be.domain.stores.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.DailySalesCalendarDto;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.MonthlySalesCalendarResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Sales;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.SalesRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SalesCalendarServiceImpl implements SalesCalendarService {

	private final SalesRepository salesRepository;

	@Override
	public MonthlySalesCalendarResponseDto getMonthlySalesCalendar(int year, int month) {

		LocalDate start = LocalDate.of(year, month, 1);
		LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

		List<Sales> salesList = salesRepository.findByDateBetween(start, end);

		Map<LocalDate, Sales> salesMap = salesList.stream()
			.collect(Collectors.toMap(Sales::getDate, s -> s));

		List<DailySalesCalendarDto> dailySales = new ArrayList<>();

		int totalSales = 0;
		int totalRefund = 0;

		for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {

			Sales sales = salesMap.get(date);

			int salesAmount = sales != null ? sales.getTotalPrice() : 0;
			int refundAmount = sales != null ? sales.getRefundPrice() : 0;

			totalSales += salesAmount;
			totalRefund += refundAmount;

			dailySales.add(
				DailySalesCalendarDto.builder()
					.date(date)
					.salesAmount(salesAmount)
					.refundAmount(refundAmount)
					.build()
			);
		}

		return MonthlySalesCalendarResponseDto.builder()
			.monthTotalSales(totalSales)
			.monthTotalRefund(totalRefund)
			.dailySales(dailySales)
			.build();
	}
}
