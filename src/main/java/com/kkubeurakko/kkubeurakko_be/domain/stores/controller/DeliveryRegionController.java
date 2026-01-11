package com.kkubeurakko.kkubeurakko_be.domain.stores.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.DeliveryRegionCreateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.DeliveryRegionResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.DeliveryRegionStatusUpdateRequest;
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

	@Operation(
		summary = "배달 지역 상태 변경",
		description = "배달 지역의 배달 가능 여부 상태를 변경합니다."
	)
	@PatchMapping("/{storeId}")
	public ResponseEntity<ApiResponse<Void>> updateDeliveryRegionStatus(
		@PathVariable Long storeId,
		@RequestBody @Valid DeliveryRegionStatusUpdateRequest request
	) {
		deliveryRegionService.updateDeliveryRegionStatus(
			storeId,
			request
		);

		return ResponseEntity.ok(
			ApiResponse.success("배달 지역 상태를 성공적으로 변경했습니다.", null)
		);
	}

	@Operation(
		summary = "배달 지역 조회",
		description = "배달 지역 전체 리스트를 조회합니다."
	)
	@GetMapping("/{storeId}")
	public ResponseEntity<ApiResponse<List<DeliveryRegionResponseDto>>> getDeliveryRegions(
		@PathVariable Long storeId
	) {
		List<DeliveryRegionResponseDto> responseDto = deliveryRegionService.getDeliveryRegions(storeId);
		return ResponseEntity.ok(
			ApiResponse.success(
				"배달 지역 리스트를 성공적으로 조회했습니다.",
				responseDto
			)
		);
	}
}