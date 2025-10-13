package com.kkubeurakko.kkubeurakko_be.global.security.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.filter.GenericFilterBean;

import com.kkubeurakko.kkubeurakko_be.global.jwt.repository.RefreshTokenRepository;
import com.kkubeurakko.kkubeurakko_be.global.jwt.util.JwtUtil;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class CustomLogoutFilter extends GenericFilterBean {

	private final JwtUtil jwtUtil;
	private final RefreshTokenRepository refreshTokenRepository;
	private final RedisTemplate<String, String> redisTemplate;
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws
		IOException,
		ServletException {
		if(request instanceof HttpServletRequest&& response instanceof HttpServletResponse) {
			doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
		} else {
			chain.doFilter(request, response);
		}
	}

	private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (!isLogoutRequest(request)) {
			chain.doFilter(request, response);
			return;
		}
		try {
			String refreshToken = extractRefreshToken(request)
				.orElseThrow(IllegalArgumentException::new);

			String accessToken = request.getHeader("Authorization");
			if (accessToken == null || !accessToken.startsWith("Bearer ")) {
				throw new IllegalArgumentException("Access 토큰이 없습니다.");
			}
			accessToken = accessToken.substring(7);

			validateRefreshToken(refreshToken);
			performLogout(accessToken, refreshToken, response);

			response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
			response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");

			log.info("로그아웃 성공: {}", refreshToken);
		} catch (IllegalArgumentException | ExpiredJwtException e) {
			log.error("잘못된 토큰이거나 토큰이 없음: {}", e.getMessage());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		} catch (Exception e) {
			log.error("로그아웃 실패", e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	private boolean isLogoutRequest(HttpServletRequest request) {
		return "/logout".equals(request.getRequestURI()) && "POST".equalsIgnoreCase(request.getMethod());
	}

	private Optional<String> extractRefreshToken(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return Optional.empty();
		}
		return Arrays.stream(cookies)
			.filter(cookie -> "Authorization".equals(cookie.getName()))
			.map(Cookie::getValue)
			.findFirst();
	}

	private void validateRefreshToken(String refreshToken) {
		if(jwtUtil.isExpired(refreshToken)) { // 만료 여부 확인
			throw new ExpiredJwtException(null, null, "refreshToken이 만료 되었습니다.");
		}
		if (!"refresh".equals(jwtUtil.getCategory(refreshToken))) {
			throw new IllegalArgumentException("잘못된 유형의 토큰");
		}
		if (!refreshTokenRepository.existsByRefresh(refreshToken)) {
			throw new IllegalArgumentException("저장된 refreshToken이 아닙니다.");
		}
	}

	private void performLogout(String accessToken, String refreshToken, HttpServletResponse response) {
		// DB에서 Refresh 토큰 삭제
		refreshTokenRepository.deleteByRefresh(refreshToken);
		// AccessToken 블랙리스트 등록
		long expiration = jwtUtil.getRemainingExpiration(accessToken);
		redisTemplate.opsForValue().set("blacklist:" + accessToken, "logout", expiration, TimeUnit.MILLISECONDS);

		log.info("로그아웃 완료: 블랙리스트 등록 & RefreshToken 삭제");
		// 브라우저 쿠키에서 Refresh 토큰 제거
		Cookie cookie = new Cookie("refresh", null);
		cookie.setMaxAge(0);
		cookie.setPath("/");
		response.addCookie(cookie);
	}
}
