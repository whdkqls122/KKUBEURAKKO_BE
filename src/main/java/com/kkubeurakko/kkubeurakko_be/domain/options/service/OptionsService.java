package com.kkubeurakko.kkubeurakko_be.domain.options.service;

import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionRequestDto;
import org.springframework.stereotype.Service;

@Service
public interface OptionsService {

    //옵션 생성
    public void createOption(OptionRequestDto requestDto);
}
