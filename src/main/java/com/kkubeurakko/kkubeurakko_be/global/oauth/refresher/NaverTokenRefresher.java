package com.kkubeurakko.kkubeurakko_be.global.oauth.refresher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.OAuthProvider;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.token.NaverTokenResponse;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.token.OAuthTokenResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NaverTokenRefresher implements OAuthTokenRefresher {

	private final RestTemplate oauthRestTemplate;

	@Value("${spring.security.oauth2.client.registration.naver.client-id}")
	private String clientId;

	@Value("${spring.security.oauth2.client.registration.naver.client-secret}")
	private String clientSecret;

	@Value("${spring.security.naver.refresh-url}")
	private String refreshUrl;

	@Override
	public OAuthProvider supports() {
		return OAuthProvider.NAVER;
	}

	@Override
	public OAuthTokenResponse refresh(String refreshToken) {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("grant_type", "refresh_token");
		params.add("client_id", clientId);
		params.add("client_secret", clientSecret);
		params.add("refresh_token", refreshToken);

		ResponseEntity<NaverTokenResponse> response =
			oauthRestTemplate.postForEntity(
				refreshUrl,
				params,
				NaverTokenResponse.class
			);

		NaverTokenResponse body = response.getBody();
		if (body.accessToken() == null) {
			throw new IllegalStateException("Naver 토큰 재발급 실패");
		}

		return body.toOAuthTokenResponse();
	}
}
