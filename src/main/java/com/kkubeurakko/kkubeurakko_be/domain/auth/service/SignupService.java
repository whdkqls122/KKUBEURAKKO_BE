package com.kkubeurakko.kkubeurakko_be.domain.auth.service;

import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.CheckDuplicateDto;
import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.SignupRequestDto;

public interface SignupService {
	//회원가입
	void signupInit(SignupRequestDto signupRequestDto);

	//아이디 중복 확인
	boolean checkDuplicatedId(CheckDuplicateDto checkDuplicateDto);
}
