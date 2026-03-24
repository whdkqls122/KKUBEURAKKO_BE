package com.kkubeurakko.kkubeurakko_be.domain.auth.service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.kkubeurakko.kkubeurakko_be.domain.users.entity.UserRole;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.service.UserService;
import com.kkubeurakko.kkubeurakko_be.global.jwt.entity.RefreshToken;
import com.kkubeurakko.kkubeurakko_be.global.jwt.repository.RefreshTokenRepository;
import com.kkubeurakko.kkubeurakko_be.global.jwt.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenServiceImpl implements TokenService {
	private final JwtUtil jwtUtil;
	private final RefreshTokenRepository refreshTokenRepository;
	private final UserService userService;
	private final CookieService cookieService;
	private final RedisTemplate<String, String> redisTemplate;

	@Value("${spring.jwt.access-token-expiry}")
	private long accessTokenExpiry;
	@Value("${spring.jwt.refresh-token-expiry}")
	private long refreshTokenExpiry;
	@Value("${spring.jwt.temp-token-expiry}")
	private long tempTokenExpiry;

	private static final String REFRESH_COOKIE_NAME = "refresh";
	private static final String TEMP_COOKIE_NAME = "temp";
	private static final String BLACKLIST_PREFIX = "blacklist:";

	@Override
	public String reissue(HttpServletRequest request) {
		log.info("Accesstoken 재발급 시작");
		String refreshToken = cookieService.getCookieValue(request, REFRESH_COOKIE_NAME);
		if (refreshToken == null || refreshToken.isBlank()) {
			throw new AccessDeniedException("Refresh Token 없음");
		}
		//토큰 유효성 검사
		jwtUtil.validateToken(refreshToken);

		//정보 추출 및 토큰 일치 검사
		String userNumber = jwtUtil.getUserNumber(refreshToken);
		isExistRefreshToken(refreshToken, userNumber);
		String role = jwtUtil.getRole(refreshToken);

		log.info("Accesstoken 재발급 성공");
		//토큰 생성 및 반환
		return jwtUtil.createAccessToken(userNumber, role, accessTokenExpiry);
	}

	@Override
	public String createTokenAfterSignup(
		HttpServletRequest request,
		HttpServletResponse response
	) {
		String tempToken = getTempToken(request);
		removeCookie(response, TEMP_COOKIE_NAME);

		String userNumber = jwtUtil.getUserNumber(tempToken);
		Users user = userService.getUser(userNumber);

		String access = jwtUtil.createAccessToken(
			user.getUserNumber(),
			user.getRole().toString(),
			accessTokenExpiry
		);
		String refresh = jwtUtil.createRefreshToken(
			user.getUserNumber(),
			user.getRole().toString(),
			refreshTokenExpiry
		);
		saveRefreshToken(userNumber, refresh);
		cookieService.addCookie(
			response,
			REFRESH_COOKIE_NAME,
			refresh,
			Duration.ofMillis(refreshTokenExpiry),
			true
		);
		return access;
	}

	@Override
	public String createLoginToken(HttpServletResponse response, String key, String role) {
		String accessToken = jwtUtil.createAccessToken(key, role, accessTokenExpiry);
		String refreshToken = jwtUtil.createRefreshToken(key, role, refreshTokenExpiry);

		saveRefreshToken(key, refreshToken);

		cookieService.addCookie(
			response,
			REFRESH_COOKIE_NAME,
			refreshToken,
			Duration.ofMillis(refreshTokenExpiry),
			true
		);

		return accessToken;
	}

	@Override
	public void createRefreshToken(HttpServletResponse response, String userNumber) {
		String refresh = jwtUtil.createRefreshToken(userNumber, UserRole.CUSTOMER.toString(), refreshTokenExpiry);

		saveRefreshToken(userNumber, refresh);

		cookieService.addCookie(
			response,
			REFRESH_COOKIE_NAME,
			refresh,
			Duration.ofMillis(refreshTokenExpiry),
			true
		);
	}

	@Override
	public String getTempToken(HttpServletRequest request) {
		return cookieService.getCookieValue(request, TEMP_COOKIE_NAME);
	}

	@Override
	public void removeTempToken(HttpServletResponse response) {
		removeCookie(response, TEMP_COOKIE_NAME);
	}

	@Override
	public String getRefreshToken(HttpServletRequest request) {
		return cookieService.getCookieValue(request, REFRESH_COOKIE_NAME);
	}

	@Override
	public void removeRefreshToken(HttpServletResponse response) {
		removeCookie(response, REFRESH_COOKIE_NAME);
	}

	@Override
	public void createTempToken(HttpServletResponse response, String userNumber) {
		String tempToken = jwtUtil.createAccessToken(userNumber, UserRole.TEMP.toString(), tempTokenExpiry);

		cookieService.addCookie(
			response,
			TEMP_COOKIE_NAME,
			tempToken,
			Duration.ofMillis(tempTokenExpiry),
			true
		);
	}

	private void isExistRefreshToken(String refreshToken, String userNumber) {
		RefreshToken findRefreshToken = refreshTokenRepository.findByUserNumber(userNumber);
		if (findRefreshToken == null || !refreshToken.equals(findRefreshToken.getRefresh())) {
			log.error("저장된 refreshToken과 불일치");
			throw new AccessDeniedException(
				"저장된 refreshToken과 일치하지 않습니다."
			);  // 로그아웃 요청을 보내도록 -> 재로그인 요청하도록(공통 예외처리 구현 이후 수정 예정)
		}
	}

	//redis에 저장
	private void saveRefreshToken(String userNumber, String refresh) {
		RefreshToken refreshToken = RefreshToken.builder()
			.userNumber(userNumber)
			.refresh(refresh)
			.build();

		refreshTokenRepository.save(refreshToken);
	}

	private void removeCookie(HttpServletResponse response, String name) {
		cookieService.removeCookie(response, name, true);
	}

	public void blacklistAccessToken(String accessToken) {
		long expiration = jwtUtil.getRemainingExpiration(accessToken);
		if (expiration <= 0) {
			return;
		}
		redisTemplate.opsForValue()
			.set(BLACKLIST_PREFIX + accessToken, "logout", expiration, TimeUnit.MILLISECONDS);
	}
}
