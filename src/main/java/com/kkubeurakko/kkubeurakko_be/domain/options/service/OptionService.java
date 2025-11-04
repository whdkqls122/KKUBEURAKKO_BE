package com.kkubeurakko.kkubeurakko_be.domain.options.service;

import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionStatus;

public interface OptionService {

	//옵션 생성
	void createOption(OptionRequestDto requestDto);

	//옵션 수정
	void updateOption(Long optionId, OptionRequestDto requestDto);

	//옵션 상태 수정
	void updateStatus(Long optionId, OptionStatus newOptionStatus);

	//옵션 삭제
	void deleteOption(Long optionId);
}
