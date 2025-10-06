package com.kkubeurakko.kkubeurakko_be.domain.options.controller;

import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.service.OptionService;
import com.kkubeurakko.kkubeurakko_be.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/menu/option")
public class OptionController {

    private final OptionService optionService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createOption(@RequestBody OptionRequestDto requestDto) {
        optionService.createOption(requestDto);
        return ResponseEntity.status(201)
                .body(ApiResponse.success("옵션 등록이 완료되었습니다.", null));
    }

}
