package com.kkubeurakko.kkubeurakko_be.domain.options.controller;

import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionGroupRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.service.OptionGroupService;
import com.kkubeurakko.kkubeurakko_be.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/menu/option-group")
public class OptionGroupController {

    private final OptionGroupService optionGroupService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createOptionGroup(@RequestBody OptionGroupRequestDto requestDto) {
        optionGroupService.createOptionGroup(requestDto);
        return ResponseEntity.status(200)
                .body(ApiResponse.success("옵션 그룹 등록이 완료되었습니다.", null));
    }
}
