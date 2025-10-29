package com.kkubeurakko.kkubeurakko_be.domain.options.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionGroupRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionGroupResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.service.OptionGroupService;
import com.kkubeurakko.kkubeurakko_be.global.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "OptionGroup API", description = "옵션 그룹 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/menu/option-group")
public class OptionGroupController {

	private final OptionGroupService optionGroupService;

	@Operation(
		summary = "옵션 그룹 생성",
		description = "새로운 옵션 그룹을 등록합니다."
	)
	@PostMapping
	public ResponseEntity<ApiResponse<Void>> createOptionGroup(@Valid @RequestBody OptionGroupRequestDto requestDto) {
		optionGroupService.createOptionGroup(requestDto);
		return ResponseEntity.status(200)
			.body(ApiResponse.success("옵션 그룹 등록이 완료되었습니다.", null));
	}

	@Operation(
		summary = "옵션 그룹 단건 조회",
		description = "옵션 그룹과 해당 그룹에 속한 옵션들을 함께 조회합니다."
	)
	@GetMapping("/{optionGroupId}")
	public ResponseEntity<ApiResponse<OptionGroupResponseDto>> getOptionGroup(@PathVariable Long optionGroupId) {
		OptionGroupResponseDto responseDto = optionGroupService.getOptionGroup(optionGroupId);
		return ResponseEntity.ok(ApiResponse.success("해당 옵션 그룹이 조회되었습니다.", responseDto));
	}

	@Operation(
		summary = "옵션 그룹 전체 조회",
		description = "모든 옵션 그룹과 해당 그룹에 속한 옵션들을 조회합니다."
	)
	@GetMapping
	public ResponseEntity<ApiResponse<List<OptionGroupResponseDto>>> getAllOptionGroups() {
		List<OptionGroupResponseDto> responseDtoList = optionGroupService.getAllOptionGroups();
		return ResponseEntity.ok(ApiResponse.success("전체 옵션 그룹이 조회되었습니다.", responseDtoList));
	}

	@Operation(summary = "옵션 그룹 수정", description = "기존 옵션 그룹 내용을 수정합니다.")
	@PutMapping("/{optionGroupId}")
	public ResponseEntity<ApiResponse<Void>> updateOptionGroup(
		@PathVariable Long optionGroupId,
		@Valid @RequestBody OptionGroupRequestDto requestDto) {
		optionGroupService.updateOptionGroup(optionGroupId, requestDto);
		return ResponseEntity.ok(ApiResponse.success("옵션 그룹 수정이 완료되었습니다.", null));
	}

	@Operation(summary = "옵션 그룹 삭제", description = "기존 옵션 그룹을 삭제합니다.")
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> deleteOptionGroup(@PathVariable Long id) {
		optionGroupService.deleteOptionGroup(id);
		return ResponseEntity.ok(ApiResponse.success("옵션 그룹 삭제가 완료되었습니다.", null));
	}
}
