package com.kkubeurakko.kkubeurakko_be.domain.stores.service;

import java.time.LocalDate;
import java.util.List;

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

		List<Sales> salesList =
			salesRepository.findByDateBetween(start, end);

		int totalSales = salesList.stream()
			.mapToInt(Sales::getTotalPrice)
			.sum();

		int totalRefund = salesList.stream()
			.mapToInt(Sales::getRefundPrice)
			.sum();

		List<DailySalesCalendarDto> dailySales = salesList.stream()
			.map(sales -> new DailySalesCalendarDto(
				sales.getDate(),
				sales.getTotalPrice(),
				sales.getRefundPrice()
			))
			.toList();

		return new MonthlySalesCalendarResponseDto(
			totalSales,
			totalRefund,
			dailySales
		);
	}
}
