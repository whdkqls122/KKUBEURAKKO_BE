package com.kkubeurakko.kkubeurakko_be.domain.options.controller;

import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionGroupRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.service.OptionGroupService;
import com.kkubeurakko.kkubeurakko_be.global.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
