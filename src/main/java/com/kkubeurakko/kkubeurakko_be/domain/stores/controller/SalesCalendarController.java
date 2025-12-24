package com.kkubeurakko.kkubeurakko_be.domain.stores.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.MonthlySalesCalendarResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.stores.service.SalesCalendarService;
import com.kkubeurakko.kkubeurakko_be.global.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sales")
public class SalesCalendarController {

	private final SalesCalendarService salesCalendarService;

	@GetMapping("/monthly")
	public ResponseEntity<ApiResponse<MonthlySalesCalendarResponseDto>> getMonthlySales(
		@RequestParam int year,
		@RequestParam int month
	) {
		MonthlySalesCalendarResponseDto responseDto = salesCalendarService.getMonthlySalesCalendar(year, month);
		return ResponseEntity.ok(ApiResponse.success("매출 달력이 조회되었습니다.", responseDto));
	}
}
