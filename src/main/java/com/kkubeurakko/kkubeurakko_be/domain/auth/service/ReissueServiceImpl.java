package com.kkubeurakko.kkubeurakko_be.domain.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import com.kkubeurakko.kkubeurakko_be.global.jwt.entity.RefreshToken;
import com.kkubeurakko.kkubeurakko_be.global.jwt.repository.RefreshTokenRepository;
import com.kkubeurakko.kkubeurakko_be.global.jwt.util.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReissueServiceImpl implements ReissueService{
	private final JwtUtil jwtUtil;
	private final RefreshTokenRepository refreshTokenRepository;
	@Value("${spring.jwt.access-token-expiry}")
	private long accessTokenExpiry;
	@Override
	public String reissue(HttpServletRequest request) {
		String refreshToken = getRefreshTokenFromCookies(request);
		//토큰 유효성 검사
		jwtUtil.validateToken(refreshToken);

		//정보 추출 및 토큰 일치 검사
		String userNumber = jwtUtil.getUserNumber(refreshToken);
		isExistRefreshToken(refreshToken, userNumber);
		String role = jwtUtil.getRole(refreshToken);

		//토큰 생성 및 반환
		return jwtUtil.createAccessToken(userNumber, role, accessTokenExpiry);
	}

	private String getRefreshTokenFromCookies(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if(cookies == null) {
			throw new AuthenticationCredentialsNotFoundException("쿠키가 없습니다.");
		}
		for (Cookie cookie : cookies) {
			if ("refresh".equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		throw new BadCredentialsException("refreshToken이 없습니다.");
	}

	private void isExistRefreshToken(String refreshToken, String userNumber){
		RefreshToken findRefreshToken = refreshTokenRepository.findByUserNumber(userNumber);
		if (findRefreshToken == null || !refreshToken.equals(findRefreshToken.getRefresh())) {
			throw new RuntimeException();  // 로그아웃 요청을 보내도록 -> 재로그인 요청하도록(공통 예외처리 구현 이후 수정 예정)
		}
	}
}
