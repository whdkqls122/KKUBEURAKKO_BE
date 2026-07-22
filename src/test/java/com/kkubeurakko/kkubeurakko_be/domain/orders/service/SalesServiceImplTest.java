package com.kkubeurakko.kkubeurakko_be.domain.orders.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.sales.SalesResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.orders.repository.StatisticsQueryRepository;

@ExtendWith(MockitoExtension.class)
class SalesServiceImplTest {
	@Mock
	private StatisticsQueryRepository statisticsQueryRepository;

	@InjectMocks
	private SalesServiceImpl salesService;

	@Test
	@DisplayName("일별 매출 통계가 정상적으로 계산되는지 검증한다.")
	void getDailySales() {
		// given
		LocalDate date = LocalDate.of(2025, 12, 16);

		when(statisticsQueryRepository.getSalesAmount(any(), any()))
			.thenReturn(10_000L)   // 오늘
			.thenReturn(8_000L);   // 어제

		when(statisticsQueryRepository.getOrderCount(any(), any()))
			.thenReturn(5L)       // 오늘
			.thenReturn(4L);      // 어제

		when(statisticsQueryRepository.getRefundAmount(any(), any()))
			.thenReturn(2_000L);

		when(statisticsQueryRepository.getDiscountAmount(any(), any()))
			.thenReturn(1_000L);

		// when
		SalesResponseDto result = salesService.getDailySales(date);

		// then
		assertEquals(10_000L, result.salesAmount());
		assertEquals(2_000L, result.salesDiff());

		assertEquals(5L, result.orderCount());
		assertEquals(1L, result.orderCountDiff());

		assertEquals(2_000L, result.refundAmount());
		assertEquals(1_000L, result.discountAmount());
	}

	@Test
	@DisplayName("주별 매출 통계가 정상적으로 계산되는지 검증한다.")
	void getWeeklySales() {
		// given
		LocalDate date = LocalDate.of(2025, 12, 16);

		when(statisticsQueryRepository.getSalesAmount(any(), any()))
			.thenReturn(70_000L)   // 이번 주
			.thenReturn(50_000L);  // 지난 주

		when(statisticsQueryRepository.getOrderCount(any(), any()))
			.thenReturn(30L)
			.thenReturn(20L);

		when(statisticsQueryRepository.getRefundAmount(any(), any()))
			.thenReturn(5_000L);

		when(statisticsQueryRepository.getDiscountAmount(any(), any()))
			.thenReturn(3_000L);

		// when
		SalesResponseDto result = salesService.getWeeklySales(date);

		// then
		assertEquals(70_000L, result.salesAmount());
		assertEquals(20_000L, result.salesDiff());

		assertEquals(30L, result.orderCount());
		assertEquals(10L, result.orderCountDiff());

		assertEquals(5_000L, result.refundAmount());
		assertEquals(3_000L, result.discountAmount());
	}

}