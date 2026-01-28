package com.kkubeurakko.kkubeurakko_be.domain.auth.service;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.service.UserService;
import com.kkubeurakko.kkubeurakko_be.global.jwt.repository.RefreshTokenRepository;
import com.kkubeurakko.kkubeurakko_be.global.jwt.util.JwtUtil;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutServiceImpl implements LogoutService {

	private final JwtUtil jwtUtil;
	private final RefreshTokenRepository refreshTokenRepository;
	private final RedisTemplate<String, String> redisTemplate;
	private final UserService userService;
	private final TokenService tokenService;

	@Override
	public void logout(
		HttpServletRequest request,
		HttpServletResponse response
	) {
		String accessToken = extractAccessToken(request);
		String refreshToken = extractRefreshToken(request)
			.orElse(null);
		String userNumber = jwtUtil.getUserNumber(accessToken);
		Users user = userService.getUser(userNumber);

		blacklistAccessToken(accessToken);

		if (refreshToken != null) {
			try {
				validateRefreshToken(user.getUserNumber(), refreshToken);
				refreshTokenRepository.deleteById(user.getUserNumber());
				log.info("RefreshToken 삭제 완료: user={}", user.getUserNumber());
			} catch (IllegalArgumentException e) {
				log.warn("RefreshToken 처리 중 문제 발생, 무시하고 진행: {}", e.getMessage());
			} catch (Exception e) {
				log.error("RefreshToken 삭제 중 예상치 못한 오류 발생, 무시하고 진행", e);
			}
		}
		tokenService.removeRefreshToken(response);
	}

	@Override
	public void forceLogout(
		CustomOAuth2User customOAuth2User,
		HttpServletRequest request,
		HttpServletResponse response
	) {
		try {
			Users user = userService.getUser(customOAuth2User.getName());
			String accessToken = extractAccessToken(request);
			String refreshToken = extractRefreshToken(request)
				.orElse(null);

			blacklistAccessToken(accessToken);

			if (refreshToken != null && refreshTokenRepository.existsById(user.getUserNumber())) {
				refreshTokenRepository.deleteById(user.getUserNumber());
			}

			tokenService.removeRefreshToken(response);
			log.info("회원 탈퇴로 인한 강제 로그아웃 완료");
		} catch (Exception e) {
			log.error("강제 로그아웃 중 오류 발생", e);
		}
	}

	private void blacklistAccessToken(String accessToken) {
		long expiration = jwtUtil.getRemainingExpiration(accessToken);
		redisTemplate.opsForValue()
			.set("blacklist:" + accessToken, "logout", expiration, TimeUnit.MILLISECONDS);
	}

	private void validateRefreshToken(String userNumber, String refreshToken) {
		if (jwtUtil.isExpired(refreshToken)) { // 만료 여부 확인
			throw new IllegalArgumentException("refreshToken 만료");
		}
		if (!"refresh".equals(jwtUtil.getCategory(refreshToken))) {
			throw new IllegalArgumentException("잘못된 유형의 토큰");
		}
		if (!refreshTokenRepository.existsById(userNumber)) {
			throw new IllegalArgumentException("저장된 refreshToken이 아닙니다.");
		}
	}

	private String extractAccessToken(HttpServletRequest request) {
		String accessToken = request.getHeader("Authorization");
		if (accessToken == null || !accessToken.startsWith("Bearer ")) {
			throw new IllegalArgumentException("Access 토큰이 없습니다.");
		}
		return accessToken.substring(7);
	}

	private Optional<String> extractRefreshToken(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return Optional.empty();
		}
		return Arrays.stream(cookies)
			.filter(cookie -> "refresh".equals(cookie.getName()))
			.map(Cookie::getValue)
			.findFirst();
	}
}
