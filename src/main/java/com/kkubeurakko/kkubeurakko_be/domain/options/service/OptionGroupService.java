package com.kkubeurakko.kkubeurakko_be.domain.options.service;

import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionGroupRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionGroupResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;

public interface OptionGroupService {

    //옵션 그룹 생성
    public void createOptionGroup(OptionGroupRequestDto requestDto);

    //옵션 그룹 단일 조회
    public OptionGroupResponseDto getOptionGroup(Long id);

    //옵션 그룹 전체 조회
    public List<OptionGroupResponseDto> getAllOptionGroups();
}
