package com.kkubeurakko.kkubeurakko_be.domain.auth.service;

import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface LogoutService {
	void logout(CustomOAuth2User customOAuth2User, String accessToken, String refreshToken); // 일반 로그아웃

	void forceLogout(CustomOAuth2User customOAuth2User, HttpServletRequest request, HttpServletResponse response);
}
