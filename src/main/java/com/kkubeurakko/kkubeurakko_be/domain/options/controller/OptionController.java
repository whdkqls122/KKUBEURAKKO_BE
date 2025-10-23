package com.kkubeurakko.kkubeurakko_be.domain.options.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.Status;
import com.kkubeurakko.kkubeurakko_be.domain.options.service.OptionService;
import com.kkubeurakko.kkubeurakko_be.global.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Option API", description = "옵션 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/menu/option")
public class OptionController {

	private final OptionService optionService;

	@Operation(
		summary = "옵션 생성",
		description = "새로운 옵션을 등록합니다."
	)
	@PostMapping
	public ResponseEntity<ApiResponse<Void>> createOption(@Valid @RequestBody OptionRequestDto requestDto) {
		optionService.createOption(requestDto);
		return ResponseEntity.status(201)
			.body(ApiResponse.success("옵션 등록이 완료되었습니다.", null));
	}

	@Operation(
		summary = "옵션 수정",
		description = "해당 ID의 옵션 정보를 수정합니다."
	)
	@PutMapping("/{optionId}")
	public ResponseEntity<ApiResponse<Void>> updateOption(
		@PathVariable Long optionId,
		@Valid @RequestBody OptionRequestDto requestDto) {

		optionService.updateOption(optionId, requestDto);
		return ResponseEntity.ok(ApiResponse.success("옵션 수정이 완료되었습니다.", null));
	}

	@Operation(
		summary = "옵션 상태 변경",
		description = "옵션의 판매 상태를 변경합니다."
	)
	@PatchMapping("/{optionId}/status")
	public ResponseEntity<ApiResponse<Void>> updateOptionStatus(
		@PathVariable Long optionId,
		@RequestParam Status status) {

		optionService.updateStatus(optionId, status);
		return ResponseEntity.ok(ApiResponse.success("옵션 상태가 변경되었습니다.", null));
	}

	@Operation(
		summary = "옵션 삭제",
		description = "해당 ID의 옵션을 삭제합니다."
	)
	@DeleteMapping("/{optionId}")
	public ResponseEntity<ApiResponse<Void>> deleteOption(@PathVariable Long optionId) {
		optionService.deleteOption(optionId);
		return ResponseEntity.ok(ApiResponse.success("옵션이 삭제되었습니다.", null));
	}
}
