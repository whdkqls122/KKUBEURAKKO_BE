package com.kkubeurakko.kkubeurakko_be.domain.options.service;

import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionRequestDto;

public interface OptionService {

	//옵션 생성
	void createOption(OptionRequestDto requestDto);
}
