package com.kkubeurakko.kkubeurakko_be.domain.auth.service;

import java.util.Optional;

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
	private final TokenService tokenService;
	private final UserService userService;
	private final CookieService cookieService;
	private static final String SAVED_ID_COOKIE_NAME = "SAVED_ID";

	@Override
	public void logout(
		HttpServletRequest request,
		HttpServletResponse response
	) {
		Optional<String> accessTokenOpt = extractAccessTokenOptional(request);
		String refreshToken = tokenService.getRefreshToken(request);

		// refreshToken 없으면 → 이미 로그아웃 상태로 간주
		if (refreshToken == null || refreshToken.isBlank()) {
			tokenService.removeRefreshToken(response);
			return;
		}
		try {
			String userNumber = jwtUtil.getUserNumber(refreshToken);

			accessTokenOpt.ifPresent(tokenService::blacklistAccessToken);
			refreshTokenRepository.deleteById(userNumber);

			log.info("로그아웃 완료 user={}", userNumber);
		} catch (Exception e) {
			log.warn("RefreshToken 파싱 실패. 쿠키만 제거합니다.", e);
		} finally {
			tokenService.removeRefreshToken(response);
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
			String refreshToken = tokenService.getRefreshToken(request);

			accessTokenOpt.ifPresent(tokenService::blacklistAccessToken);

			if (refreshToken != null && !refreshToken.isBlank()
				&& refreshTokenRepository.existsById(user.getUserNumber())) {
				refreshTokenRepository.deleteById(user.getUserNumber());
			}
			log.info("회원 탈퇴로 인한 강제 로그아웃 완료");
		} catch (Exception e) {
			log.error("강제 로그아웃 중 오류 발생", e);
		} finally {
			tokenService.removeRefreshToken(response);
			removeSavedIdCookie(response);
		}
	}

	private Optional<String> extractAccessTokenOptional(HttpServletRequest request) {
		String accessToken = request.getHeader("Authorization");
		if (accessToken == null || !accessToken.startsWith("Bearer ")) {
			return Optional.empty();
		}
		return Optional.of(accessToken.substring(7));
	}

	private void removeSavedIdCookie(HttpServletResponse response) {
		cookieService.removeCookie(response, SAVED_ID_COOKIE_NAME, false);
	}
}
