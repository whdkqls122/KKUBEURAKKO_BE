package com.kkubeurakko.kkubeurakko_be.domain.auth.service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.service.UserService;
import com.kkubeurakko.kkubeurakko_be.global.jwt.repository.RefreshTokenRepository;
import com.kkubeurakko.kkubeurakko_be.global.jwt.util.JwtUtil;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

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
	private final CookieService cookieService;
	private static final String REFRESH_COOKIE_NAME = "refresh";
	private static final String SAVED_ID_COOKIE_NAME = "SAVED_ID";
	private static final String BLACKLIST_PREFIX = "blacklist:";

	@Override
	public void logout(
		HttpServletRequest request,
		HttpServletResponse response
	) {
		Optional<String> accessTokenOpt = extractAccessTokenOptional(request);
		String refreshToken = cookieService.getCookieValue(request, REFRESH_COOKIE_NAME);

		// refreshToken 없으면 → 이미 로그아웃 상태로 간주
		if (refreshToken == null || refreshToken.isBlank()) {
			cookieService.removeCookie(response, REFRESH_COOKIE_NAME, true);
			return;
		}
		try {
			String userNumber = jwtUtil.getUserNumber(refreshToken);

			accessTokenOpt.ifPresent(this::blacklistAccessToken);
			refreshTokenRepository.deleteById(userNumber);

			log.info("로그아웃 완료 user={}", userNumber);
		} catch (Exception e) {
			log.warn("RefreshToken 파싱 실패. 쿠키만 제거합니다.", e);
		} finally {
			cookieService.removeCookie(response, REFRESH_COOKIE_NAME, true);
		}
	}

	@Override
	public void forceLogout(
		CustomOAuth2User customOAuth2User,
		HttpServletRequest request,
		HttpServletResponse response
	) {
		try {
			Users user = userService.getUser(customOAuth2User.getName());
			Optional<String> accessTokenOpt = extractAccessTokenOptional(request);
			String refreshToken = cookieService.getCookieValue(request, REFRESH_COOKIE_NAME);

			accessTokenOpt.ifPresent(this::blacklistAccessToken);

			if (refreshToken != null && !refreshToken.isBlank()
				&& refreshTokenRepository.existsById(user.getUserNumber())) {
				refreshTokenRepository.deleteById(user.getUserNumber());
			}
			log.info("회원 탈퇴로 인한 강제 로그아웃 완료");
		} catch (Exception e) {
			log.error("강제 로그아웃 중 오류 발생", e);
		} finally {
			cookieService.removeCookie(response, SAVED_ID_COOKIE_NAME, false);
			cookieService.removeCookie(response, REFRESH_COOKIE_NAME, true);
		}
	}

	private void blacklistAccessToken(String accessToken) {
		long expiration = jwtUtil.getRemainingExpiration(accessToken);
		if (expiration <= 0) {
			return;
		}
		redisTemplate.opsForValue()
			.set(BLACKLIST_PREFIX + accessToken, "logout", expiration, TimeUnit.MILLISECONDS);
	}

	private Optional<String> extractAccessTokenOptional(HttpServletRequest request) {
		String accessToken = request.getHeader("Authorization");
		if (accessToken == null || !accessToken.startsWith("Bearer ")) {
			return Optional.empty();
		}
		return Optional.of(accessToken.substring(7));
	}
}
