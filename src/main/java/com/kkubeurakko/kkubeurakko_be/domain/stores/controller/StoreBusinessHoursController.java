package com.kkubeurakko.kkubeurakko_be.domain.stores.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.StoreBusinessHoursRequest;
import com.kkubeurakko.kkubeurakko_be.domain.stores.service.StoreBusinessHoursService;
import com.kkubeurakko.kkubeurakko_be.global.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/store")
@Tag(name = "StoreBusinessHours API", description = "영업시간 API")
public class StoreBusinessHoursController {

	private final StoreBusinessHoursService storeBusinessHoursService;

	@Operation(
		summary = "영업시간 설정",
		description = "가게의 영업시간을 설정합니다."
	)
	@PostMapping("/{storeId}/operations/time")
	public ResponseEntity<ApiResponse<Void>> saveBusinessHours(
		@PathVariable Long storeId,
		@RequestBody StoreBusinessHoursRequest request
	) {
		storeBusinessHoursService.saveBusinessHours(storeId, request);
		return ResponseEntity.ok(ApiResponse.success("영업 시간 설정이 완료되었습니다.", null));
	}
}
