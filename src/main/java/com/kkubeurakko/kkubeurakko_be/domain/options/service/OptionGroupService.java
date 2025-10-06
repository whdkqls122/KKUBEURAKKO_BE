package com.kkubeurakko.kkubeurakko_be.domain.options.service;

import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionGroupRequestDto;
import org.springframework.stereotype.Service;

@Service
public interface OptionGroupService {

    //옵션 그룹 생성
    public void createOptionGroup(OptionGroupRequestDto requestDto);

}
