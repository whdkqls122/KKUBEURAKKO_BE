package com.kkubeurakko.kkubeurakko_be.domain.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface TokenService {
	//accessToken 재발급
	String reissue(HttpServletRequest request);

	//로그인 이후 토큰 생성 (access, refresh)
	String createToken(HttpServletRequest request, HttpServletResponse response);

	// 임시 사용자 토큰 생성 (유효기간 10분 쿠키 생성)
	void createTempToken(HttpServletResponse response, String userNumber);

	// refreshToken 생성
	void createRefreshToken(HttpServletResponse response, String userNumber);

	// 임시 사용자 토큰 조회
	String getTempToken(HttpServletRequest request);

	// 인증 완료 및 사용자 정보 저장 이후 임시 사용자 토큰 삭제
	void removeTempToken(HttpServletResponse response);

	String getRefreshToken(HttpServletRequest request);

	// 로그아웃 또는 탈퇴 시 refresh 토큰 쿠키 삭제
	void removeRefreshToken(HttpServletResponse response);

	void blacklistAccessToken(String accessToken);
}
