package com.kkubeurakko.kkubeurakko_be.domain.stores.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.DeliveryRegionCreateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.stores.service.DeliveryRegionService;
import com.kkubeurakko.kkubeurakko_be.global.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/delivery")
@Tag(name = "DeliveryRegion API", description = "배달 지역 API")
public class DeliveryRegionController {

	private final DeliveryRegionService deliveryRegionService;

	@Operation(
		summary = "배달 가능 지역 저장",
		description = "배달 가능한 지역을 저장합니다."
	)
	@PostMapping("/{storeId}")
	public ResponseEntity<ApiResponse<Void>> saveDeliveryRegion(
		@PathVariable Long storeId,
		@RequestBody @Valid DeliveryRegionCreateRequest request
	) {
		deliveryRegionService.saveDeliveryRegion(storeId, request);

		return ResponseEntity.ok(
			ApiResponse.success("배달 지역을 성공적으로 저장했습니다.", null)
		);
	}
}