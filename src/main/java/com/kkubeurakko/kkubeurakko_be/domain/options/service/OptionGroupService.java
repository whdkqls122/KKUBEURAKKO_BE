package com.kkubeurakko.kkubeurakko_be.domain.options.service;

import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionGroupNameResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionGroupRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionGroupResponseDto;

public interface OptionGroupService {

	//옵션 그룹 생성
	void createOptionGroup(OptionGroupRequestDto requestDto);

	//옵션 그룹 단일 조회
	OptionGroupResponseDto getOptionGroup(Long id);

	//옵션 그룹 전체 조회
	List<OptionGroupResponseDto> getAllOptionGroups();

	List<OptionGroupNameResponseDto> getAllOptionGroupName();

	//옵션 그룹 수정
	void updateOptionGroup(Long id, OptionGroupRequestDto requestDto);

	//옵션 그룹 삭제
	void deleteOptionGroup(Long id);
}
