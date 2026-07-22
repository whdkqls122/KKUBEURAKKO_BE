package com.kkubeurakko.kkubeurakko_be.domain.auth.service;

import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.ExistUserRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.NextStepDto;
import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.ResetPasswordMeRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.ResetPasswordRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.UserIdResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.dto.UserVerifyRequestDto;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AccountService {
	//사용자 아이디 찾기(마스킹 후 리턴)
	UserIdResponseDto findUserId(UserVerifyRequestDto verifyRequestDto);

	//비밀번호 재설정을 위한 사용자 유무 확인
	void existUser(HttpServletResponse response, ExistUserRequestDto existUserRequestDto);

	// 모바일 본인인증 이후 다음단계 조회
	NextStepDto getNextStep(HttpServletRequest request);

	//비밀번호 재설정 전 본인인증
	void verifyForResetPassword(
		HttpServletRequest request,
		HttpServletResponse response,
		UserVerifyRequestDto verifyRequestDto
	);

	//비밀번호 재설정
	void resetPassword(
		HttpServletRequest request,
		HttpServletResponse response,
		ResetPasswordRequestDto resetPasswordRequestDto
	);

	void resetPasswordMe(CustomOAuth2User customOAuth2User, ResetPasswordMeRequestDto resetPasswordRequestDto);

}
