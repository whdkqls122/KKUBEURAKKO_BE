package com.kkubeurakko.kkubeurakko_be.domain.options.controller;

import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionGroupRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionGroupResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.service.OptionGroupService;
import com.kkubeurakko.kkubeurakko_be.global.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "OptionGroup API", description = "메뉴 옵션 그룹 관련 API")
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
        return ResponseEntity.ok(ApiResponse.success("옵션 그룹 조회 성공", responseDto));
    }

    @Operation(
            summary = "옵션 그룹 전체 조회",
            description = "모든 옵션 그룹과 해당 그룹에 속한 옵션들을 조회합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<OptionGroupResponseDto>>> getAllOptionGroups() {
        List<OptionGroupResponseDto> responseDtoList = optionGroupService.getAllOptionGroups();
        return ResponseEntity.ok(ApiResponse.success("전체 옵션 그룹 조회 성공", responseDtoList));
    }
}
