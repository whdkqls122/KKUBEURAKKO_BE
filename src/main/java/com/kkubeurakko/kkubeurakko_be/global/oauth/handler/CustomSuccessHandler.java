package com.kkubeurakko.kkubeurakko_be.global.oauth.handler;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkubeurakko.kkubeurakko_be.global.jwt.entity.RefreshToken;
import com.kkubeurakko.kkubeurakko_be.global.jwt.repository.RefreshTokenRepository;
import com.kkubeurakko.kkubeurakko_be.global.jwt.util.JwtUtil;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomSuccessHandler implements AuthenticationSuccessHandler {
	private final JwtUtil jwtUtil;
	private final RefreshTokenRepository refreshTokenRepository;
	private final ObjectMapper objectMapper;

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

		boolean firstLogin = customUserDetails.getUserDto().isFirstLogin();

		String refresh = jwtUtil.createRefreshToken(userNumber, role, refreshTokenExpiry);

		saveRefreshToken(userNumber, refresh);

		response.addHeader("Set-Cookie", createCookie(refresh).toString());

		String redirectUrl = "http://localhost:5173/home?success=" + true + "&firstLogin=" + firstLogin;
		//실사용 url
		// String redirectUrl = "https://www.kkubeurakko-anyang.shop/home?success=" + true + "&firstLogin=" + firstLogin;
		log.info("로그인 성공");
		response.sendRedirect(redirectUrl);
	}

	private ResponseCookie createCookie(String value) {
		return ResponseCookie.from("refresh", value)
			.maxAge(refreshTokenExpiry / 1000)
			.secure(true)
			// .httpOnly(true)
			.sameSite("None")
			.path("/")
			// .domain(".kkubeurakko-anyang.shop")
			.build();
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
