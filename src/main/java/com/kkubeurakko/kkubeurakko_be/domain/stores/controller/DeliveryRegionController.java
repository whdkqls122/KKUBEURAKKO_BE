package com.kkubeurakko.kkubeurakko_be.domain.stores.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.DeliveryRegionAbleResponse;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.DeliveryRegionRequest;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.DeliveryRegionResponse;
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
		description = "배달 가능한 지역 반경 거리를 저장합니다."
	)
	@PostMapping("/{storeId}")
	public ResponseEntity<ApiResponse<Void>> saveDeliveryRegion(
		@PathVariable Long storeId,
		@RequestBody @Valid DeliveryRegionRequest request
	) {
		deliveryRegionService.saveDeliveryRegion(storeId, request);

		return ResponseEntity.ok(
			ApiResponse.success("배달 지역을 성공적으로 저장했습니다.", null)
		);
	}

	@Operation(
		summary = "배달 지역 반경 거리 수정",
		description = "배달 가능한 지역 반경 거리를 수정합니다."
	)
	@PatchMapping("/{storeId}")
	public ResponseEntity<ApiResponse<Void>> updateDeliveryRegion(
		@PathVariable Long storeId,
		@RequestBody @Valid DeliveryRegionRequest request
	) {
		deliveryRegionService.updateDeliveryRegion(
			storeId,
			request
		);

		return ResponseEntity.ok(
			ApiResponse.success("배달 반경 거리 수정이 완료되었습니다.", null)
		);
	}

	@Operation(
		summary = "배달 가능 지역 정보 조회",
		description = "지도 출력을 위해 설정 반경 및 가게 좌표를 조회합니다."
	)
	@GetMapping("/{storeId}")
	public ResponseEntity<ApiResponse<DeliveryRegionResponse>> getDeliveryRegion(
		@PathVariable Long storeId
	) {

		DeliveryRegionResponse deliveryRegionResponse = deliveryRegionService.getDeliveryRegion(storeId);
		return ResponseEntity.ok(
			ApiResponse.success(
				"배달 가능 지역 정보 조회가 완료되었습니다.",
				deliveryRegionResponse
			)
		);
	}

	@Operation(
		summary = "배달 가능 여부 판단",
		description = "좌표 기반으로 배달 가능 여부를 판단합니다."
	)
	@GetMapping("/stores/{storeId}/delivery-availability")
	public ResponseEntity<ApiResponse<DeliveryRegionAbleResponse>> checkDeliveryAvailability(
		@PathVariable Long storeId,
		@RequestParam double latitude,
		@RequestParam double longitude
	) {

		DeliveryRegionAbleResponse deliverable = deliveryRegionService.isDeliverable(storeId, latitude, longitude);

		return ResponseEntity.ok(
			ApiResponse.success(
				"배달 가능 여부 조회 성공",
				deliverable
			)
		);
	}
}