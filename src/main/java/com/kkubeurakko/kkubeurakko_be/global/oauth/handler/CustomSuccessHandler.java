package com.kkubeurakko.kkubeurakko_be.global.oauth.handler;

import java.io.IOException;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.kkubeurakko.kkubeurakko_be.domain.auth.service.TokenService;
import com.kkubeurakko.kkubeurakko_be.global.jwt.repository.RefreshTokenRepository;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomSuccessHandler implements AuthenticationSuccessHandler {
	private final RefreshTokenRepository refreshTokenRepository;
	private final OAuth2AuthorizedClientService authorizedClientService;
	private final RedisTemplate<String, String> redisTemplate;
	private final TokenService tokenService;

	@Override
	public void onAuthenticationSuccess(
		HttpServletRequest request,
		HttpServletResponse response,
		Authentication authentication)
		throws IOException {
		OAuth2AuthenticationToken oAuthToken = (OAuth2AuthenticationToken)authentication;

		CustomOAuth2User customUserDetails = (CustomOAuth2User)oAuthToken.getPrincipal();
		String userNumber = customUserDetails.getName();
		String role = authentication.getAuthorities().iterator().next().getAuthority();

		String provider =
			oAuthToken.getAuthorizedClientRegistrationId();

		OAuth2AuthorizedClient authorizedClient =
			authorizedClientService.loadAuthorizedClient(
				provider,
				oAuthToken.getName()
			);

		String providerAccessToken =
			authorizedClient.getAccessToken().getTokenValue();
		refreshTokenRepository.deleteById(userNumber);

		String providerRefreshToken =
			authorizedClient.getRefreshToken() != null
				? authorizedClient.getRefreshToken().getTokenValue()
				: null;

		saveOAuthToken(provider, userNumber, providerAccessToken, providerRefreshToken);
		String redirectUrl;

		boolean firstLogin = customUserDetails.getUserDto().isFirstLogin();
		if (firstLogin) {
			tokenService.createTempToken(response, userNumber);
			redirectUrl = "http://localhost:5173/verify/agreement";
			// redirectUrl = "https://www.kkubeurakko-anyang.shop/verify/agreement";
		} else {
			tokenService.createRefreshToken(response, userNumber);
			redirectUrl = "http://localhost:5173/home?success=" + true;
			// redirectUrl = "https://www.kkubeurakko-anyang.shop/home?success=" + true;
		}
		log.info("로그인 성공");
		response.sendRedirect(redirectUrl);
	}

	private void saveOAuthToken(
		String provider,
		String userNumber,
		String providerAccessToken,
		String providerRefreshToken
	) {
		redisTemplate.opsForValue().set(
			"OAUTH:AT:" + userNumber,
			providerAccessToken
		);
		redisTemplate.opsForValue().set(
			"OAUTH:RT:" + userNumber,
			providerRefreshToken
		);
		redisTemplate.opsForValue().set(
			"OAUTH:PROVIDER:" + userNumber,
			provider
		);
	}
}
