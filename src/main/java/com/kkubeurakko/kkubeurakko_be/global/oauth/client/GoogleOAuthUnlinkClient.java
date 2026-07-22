package com.kkubeurakko.kkubeurakko_be.global.oauth.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.OAuthProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleOAuthUnlinkClient implements OAuthUnlinkClient {
	@Value("${spring.security.google.unlink-url}")
	private String unlinkUrl;

	private final RestTemplate oauthRestTemplate;

	@Override
	public OAuthProvider supports() {
		return OAuthProvider.GOOGLE;
	}

	@Override
	public void unlink(String accessToken) {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("token", accessToken);

		oauthRestTemplate.postForEntity(
			unlinkUrl,
			params,
			String.class
		);
	}
}

