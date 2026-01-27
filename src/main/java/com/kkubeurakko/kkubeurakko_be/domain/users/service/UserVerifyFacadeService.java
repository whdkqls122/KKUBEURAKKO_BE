package com.kkubeurakko.kkubeurakko_be.domain.users.service;

import com.kkubeurakko.kkubeurakko_be.domain.users.dto.UserVerifyRequestDto;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface UserVerifyFacadeService {
	//사용자 인증 정보 조회 후 사용자 저장
	void verifyAndSaveUser(
		UserVerifyRequestDto userVerifyRequestDto,
		HttpServletRequest request,
		HttpServletResponse response
	);

	void deleteUserAndUnlink(
		CustomOAuth2User customOAuth2User,
		HttpServletRequest request,
		HttpServletResponse response
	);
}
