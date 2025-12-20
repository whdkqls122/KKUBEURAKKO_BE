package com.kkubeurakko.kkubeurakko_be.global.oauth.refresher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.OAuthProvider;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.token.KakaoTokenResponse;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.token.OAuthTokenResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class KakaoTokenRefresher implements OAuthTokenRefresher {

	private final RestTemplate oauthRestTemplate;

	@Value("${spring.security.oauth2.client.registration.kakao.client-id}")
	private String clientId;

	@Value("${spring.security.kakao.refresh-url}")
	private String refreshUrl;

	@Override
	public OAuthProvider supports() {
		return OAuthProvider.KAKAO;
	}

	@Override
	public OAuthTokenResponse refresh(String refreshToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("grant_type", "refresh_token");
		params.add("client_id", clientId);
		params.add("refresh_token", refreshToken);

		HttpEntity<?> request = new HttpEntity<>(params, headers);

		ResponseEntity<KakaoTokenResponse> response =
			oauthRestTemplate.postForEntity(
				refreshUrl,
				request,
				KakaoTokenResponse.class
			);

		KakaoTokenResponse body = response.getBody();
		if (body.accessToken() == null) {
			throw new IllegalStateException("Kakao 토큰 재발급 실패");
		}

		return body.toOAuthTokenResponse();
	}
}
