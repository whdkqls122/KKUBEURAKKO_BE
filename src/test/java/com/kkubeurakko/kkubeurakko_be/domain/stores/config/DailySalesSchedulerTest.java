package com.kkubeurakko.kkubeurakko_be.domain.stores.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kkubeurakko.kkubeurakko_be.domain.orders.repository.StatisticsQueryRepository;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Sales;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.SalesRepository;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.StoreRepository;
import com.kkubeurakko.kkubeurakko_be.domain.stores.service.DailySalesBatchService;

@ExtendWith(MockitoExtension.class)
class DailySalesSchedulerTest {

	@Mock
	private StatisticsQueryRepository statisticsQueryRepository;

	@Mock
	private SalesRepository salesRepository;

	@Mock
	private StoreRepository storeRepository;

	@InjectMocks
	private DailySalesBatchService dailySalesBatchService;

	@Test
	@DisplayName("하루 매출 스케줄링 시 매출 데이터가 정상 저장된다")
	void saveDailySales() {
		// given
		LocalDate targetDate = LocalDate.of(2025, 12, 16);

		Stores store = Stores.builder()
			.id(1L)
			.build();

		when(salesRepository.existsByDate(targetDate))
			.thenReturn(false);

		when(statisticsQueryRepository.getSalesAmount(any(), any()))
			.thenReturn(15000L);

		when(statisticsQueryRepository.getRefundAmount(any(), any()))
			.thenReturn(3000L);

		when(storeRepository.findById(1L))
			.thenReturn(Optional.of(store));

		// when
		dailySalesBatchService.saveDailySales(targetDate);

		// then
		ArgumentCaptor<Sales> salesCaptor = ArgumentCaptor.forClass(Sales.class);
		verify(salesRepository).save(salesCaptor.capture());

		Sales savedSales = salesCaptor.getValue();

		assertEquals(targetDate, savedSales.getDate());
		assertEquals(15000, savedSales.getTotalPrice());
		assertEquals(3000, savedSales.getRefundPrice());
		assertEquals(store, savedSales.getStore());
	}

}