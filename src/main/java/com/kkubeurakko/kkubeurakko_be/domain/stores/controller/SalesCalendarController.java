package com.kkubeurakko.kkubeurakko_be.domain.stores.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.MonthlySalesCalendarResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.stores.service.SalesCalendarService;
import com.kkubeurakko.kkubeurakko_be.global.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sales")
@Tag(name = "Sales API", description = "매출 달력 API")
public class SalesCalendarController {

	private final SalesCalendarService salesCalendarService;

	@Operation(
		summary = "매출 달력 데이터 조회",
		description = "달력에 표시할 매출 데이터를 조회합니다."
	)
	@GetMapping("/monthly")
	public ResponseEntity<ApiResponse<MonthlySalesCalendarResponseDto>> getMonthlySales(
		@RequestParam int year,
		@RequestParam int month
	) {
		MonthlySalesCalendarResponseDto responseDto = salesCalendarService.getMonthlySalesCalendar(year, month);
		return ResponseEntity.ok(ApiResponse.success("매출 달력이 조회되었습니다.", responseDto));
	}
}
