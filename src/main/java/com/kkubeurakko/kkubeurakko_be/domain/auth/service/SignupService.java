package com.kkubeurakko.kkubeurakko_be.domain.auth.service;

import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.CheckDuplicateDto;
import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.SignupRequestDto;
import com.kkubeurakko.kkubeurakko_be.global.ApiResponse;

import jakarta.servlet.http.HttpServletResponse;

public interface SignupService {
	//회원가입
	void signupInit(HttpServletResponse response, SignupRequestDto signupRequestDto);

	//아이디 중복 확인
	ApiResponse<Void> checkDuplicatedId(CheckDuplicateDto checkDuplicateDto);
}
