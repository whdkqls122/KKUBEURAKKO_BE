package com.kkubeurakko.kkubeurakko_be.global.oauth.validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.OAuthProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoogleTokenValidator implements OAuthTokenValidator {

	private final RestTemplate restTemplate;

	@Value("${spring.security.google.validate-token-url}")
	private String validateUrl;

	@Override
	public OAuthProvider supports() {
		return OAuthProvider.GOOGLE;
	}

	@Override
	public boolean validate(String accessToken) {
		try {
			String url = validateUrl + "?access_token=" + accessToken;

			restTemplate.getForEntity(url, String.class);

			return true;
		} catch (Exception e) {
			return false;
		}
	}
}