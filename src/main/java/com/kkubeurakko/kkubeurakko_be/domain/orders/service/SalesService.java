package com.kkubeurakko.kkubeurakko_be.domain.orders.service;

import java.time.LocalDate;

import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.sales.SalesResponseDto;

public interface SalesService {

	//일별 매출 통계
	SalesResponseDto getDailySales(LocalDate date);

	//주별 매출 통계
	SalesResponseDto getWeeklySales(LocalDate date);
}
