package com.kkubeurakko.kkubeurakko_be.global.oauth.refresher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.OAuthProvider;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.token.GoogleTokenResponse;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.token.OAuthTokenResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GoogleTokenRefresher implements OAuthTokenRefresher {

	private final RestTemplate oauthRestTemplate;

	@Value("${spring.security.oauth2.client.registration.google.client-id}")
	private String clientId;

	@Value("${spring.security.oauth2.client.registration.google.client-secret}")
	private String clientSecret;

	@Value("${spring.security.google.refresh-url}")
	private String refreshUrl;

	@Override
	public OAuthProvider supports() {
		return OAuthProvider.GOOGLE;
	}

	@Override
	public OAuthTokenResponse refresh(String refreshToken) {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("client_id", clientId);
		params.add("client_secret", clientSecret);
		params.add("grant_type", "refresh_token");
		params.add("refresh_token", refreshToken);

		ResponseEntity<GoogleTokenResponse> response =
			oauthRestTemplate.postForEntity(
				refreshUrl,
				params,
				GoogleTokenResponse.class
			);

		GoogleTokenResponse body = response.getBody();
		if (body.accessToken() == null) {
			throw new IllegalStateException("Google 토큰 재발급 실패");
		}

		return body.toOAuthTokenResponse();
	}
}
