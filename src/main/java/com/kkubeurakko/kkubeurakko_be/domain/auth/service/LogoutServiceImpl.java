package com.kkubeurakko.kkubeurakko_be.domain.auth.service;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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
		Optional<String> accessTokenOpt = extractAccessTokenOptional(request);
		Optional<String> refreshTokenOpt = extractRefreshToken(request);

		// refreshToken 없으면 → 이미 로그아웃 상태로 간주
		if (refreshTokenOpt.isEmpty()) {
			tokenService.removeRefreshToken(response);
			return;
		}

		String refreshToken = refreshTokenOpt.get();
		String userNumber = jwtUtil.getUserNumber(refreshToken);

		// AccessToken 있으면 블랙리스트
		accessTokenOpt.ifPresent(this::blacklistAccessToken);

		// RefreshToken 삭제
		refreshTokenRepository.deleteById(userNumber);
		tokenService.removeRefreshToken(response);

		log.info("로그아웃 완료 user={}", userNumber);

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
			String refreshToken = extractRefreshToken(request)
				.orElse(null);

			accessTokenOpt.ifPresent(this::blacklistAccessToken);

			if (refreshToken != null && refreshTokenRepository.existsById(user.getUserNumber())) {
				refreshTokenRepository.deleteById(user.getUserNumber());
			}
			removeSavedIdCookie(response);
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

	private Optional<String> extractAccessTokenOptional(HttpServletRequest request) {
		String accessToken = request.getHeader("Authorization");
		if (accessToken == null || !accessToken.startsWith("Bearer ")) {
			return Optional.empty();
		}
		return Optional.of(accessToken.substring(7));
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

	private void removeSavedIdCookie(HttpServletResponse response) {
		ResponseCookie deleteSavedIdCookie = ResponseCookie.from("SAVED_ID", "")
			.maxAge(0)
			.path("/")
			// .domain(".kkubeurakko-anyang.shop")
			.secure(true)
			.httpOnly(false)
			.sameSite("None")
			.build();

		response.addHeader(HttpHeaders.SET_COOKIE, deleteSavedIdCookie.toString());
	}
}
