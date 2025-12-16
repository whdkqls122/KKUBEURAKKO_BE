package com.kkubeurakko.kkubeurakko_be.domain.orders.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.sales.DailySalesResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.orders.service.DailySalesService;
import com.kkubeurakko.kkubeurakko_be.global.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin/statistics")
public class DailySalesController {

	private final DailySalesService dailySalesService;

	@GetMapping("/daily")
	public ResponseEntity<ApiResponse<DailySalesResponseDto>> getDailyStatistics(
		@RequestParam("date")
		@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date
	) {
		DailySalesResponseDto dailySalesResponseDto = dailySalesService.getDailySales(date);

		return ResponseEntity.ok().body(
			ApiResponse.success(
				"특정일 매출 조회가 완료되었습니다.",
				dailySalesResponseDto
			)
		);

	}
}