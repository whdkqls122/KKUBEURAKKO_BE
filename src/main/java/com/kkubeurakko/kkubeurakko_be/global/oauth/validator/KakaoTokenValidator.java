package com.kkubeurakko.kkubeurakko_be.global.oauth.validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.OAuthProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KakaoTokenValidator implements OAuthTokenValidator {

	private final RestTemplate restTemplate;

	@Value("${spring.security.kakao.validate-token-url}")
	private String validateUrl;

	@Override
	public OAuthProvider supports() {
		return OAuthProvider.KAKAO;
	}

	@Override
	public boolean validate(String accessToken) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setBearerAuth(accessToken);

			HttpEntity<Void> entity = new HttpEntity<>(headers);

			restTemplate.exchange(
				validateUrl,
				HttpMethod.GET,
				entity,
				String.class
			);

			return true;
		} catch (Exception e) {
			return false;
		}
	}
}