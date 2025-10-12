package com.kkubeurakko.kkubeurakko_be.global.oauth.handler;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkubeurakko.kkubeurakko_be.domain.users.repository.UserRepository;
import com.kkubeurakko.kkubeurakko_be.global.jwt.entity.RefreshToken;
import com.kkubeurakko.kkubeurakko_be.global.jwt.repository.RefreshTokenRepository;
import com.kkubeurakko.kkubeurakko_be.global.jwt.util.JwtUtil;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler implements AuthenticationSuccessHandler {
	private final JwtUtil jwtUtil;
	private final RefreshTokenRepository refreshTokenRepository;
	private final ObjectMapper objectMapper;

	private final UserRepository userRepository;

	@Value("${spring.jwt.access-token-expiry}")
	private long accessTokenExpiry;

	@Value("${spring.jwt.refresh-token-expiry}")
	private long refreshTokenExpiry;

	@Override
	public void onAuthenticationSuccess(
		HttpServletRequest request,
		HttpServletResponse response,
		Authentication authentication)
		throws IOException {
		CustomOAuth2User customUserDetails = (CustomOAuth2User)authentication.getPrincipal();
		String userNumber = customUserDetails.getName();
		String role = authentication.getAuthorities().iterator().next().getAuthority();

		refreshTokenRepository.deleteById(userNumber);

		boolean firstLogin = userRepository.existsByUserNumber(userNumber);

		String access = jwtUtil.createAccessToken(userNumber, role, accessTokenExpiry);
		String refresh = jwtUtil.createRefreshToken(userNumber, role, refreshTokenExpiry);

		saveRefreshToken(userNumber, refresh);

		response.addCookie(createCookie(refresh));

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Authorization", access);
		Map<String, Object> tokenResponse = Map.of(
			"firstLogin", firstLogin
		);

		// objectMapper로 바로 쓰기
		objectMapper.writeValue(response.getWriter(), tokenResponse);
	}

	private Cookie createCookie(String value) {
		Cookie cookie = new Cookie("refresh", value);
		cookie.setMaxAge((int)refreshTokenExpiry / 1000); // 7일
		// cookie.setSecure(true);	//https
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		return cookie;
	}

	//redis에 저장
	private void saveRefreshToken(String userNumber, String refresh) {
		RefreshToken refreshToken = RefreshToken.builder()
			.userNumber(userNumber)
			.refresh(refresh)
			.build();

		refreshTokenRepository.save(refreshToken);
	}
}
