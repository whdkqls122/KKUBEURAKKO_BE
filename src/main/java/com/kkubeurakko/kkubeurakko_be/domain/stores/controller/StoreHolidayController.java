package com.kkubeurakko.kkubeurakko_be.domain.stores.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.OnceHolidayCreateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.StoreHolidayResponse;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.WeeklyHolidayCreateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.stores.service.StoreHolidayService;
import com.kkubeurakko.kkubeurakko_be.global.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/store/{storeId}/holidays")
@RequiredArgsConstructor
@Tag(name = "StoreHoliday API", description = "가게 휴무일 설정 API")
public class StoreHolidayController {

	private final StoreHolidayService holidayService;

	@Operation(
		summary = "단일 휴무일 설정",
		description = "단일 휴무일을 설정합니다."
	)
	@PostMapping("")
	public ResponseEntity<ApiResponse<Void>> createOnceHoliday(
		@PathVariable Long storeId,
		@Valid @RequestBody OnceHolidayCreateRequest request
	) {
		holidayService.createOnceHoliday(storeId, request);
		return ResponseEntity.ok(ApiResponse.success("단일 휴무가 등록되었습니다.", null));
	}

	@Operation(
		summary = "반복 휴무일 설정",
		description = "특정 요일마다 반복되는 휴무일로 설정합니다."
	)
	@PostMapping("/weekly")
	public ResponseEntity<ApiResponse<Void>> createWeeklyHoliday(
		@PathVariable Long storeId,
		@Valid @RequestBody WeeklyHolidayCreateRequest request
	) {
		holidayService.createWeeklyHoliday(storeId, request);
		return ResponseEntity.ok(ApiResponse.success("반복 휴무가 등록되었습니다.", null));
	}

	@Operation(
		summary = "휴무일 전체 조회",
		description = "가게의 휴무일 정보를 전체 조회합니다."
	)
	@GetMapping("/holidays")
	public ResponseEntity<ApiResponse<List<StoreHolidayResponse>>> getHolidays(
		@PathVariable Long storeId
	) {
		List<StoreHolidayResponse> responseDto = holidayService.getHolidays(storeId);
		return ResponseEntity.ok(ApiResponse.success("휴무일이 조회되었습니다.", responseDto));
	}

}
