package com.kkubeurakko.kkubeurakko_be.domain.options.controller;

import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.service.OptionService;
import com.kkubeurakko.kkubeurakko_be.global.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "OptionGroup API", description = "메뉴 옵션 관련 API")
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

}
