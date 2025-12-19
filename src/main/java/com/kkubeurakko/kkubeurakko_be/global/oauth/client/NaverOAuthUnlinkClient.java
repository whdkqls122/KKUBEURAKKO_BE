package com.kkubeurakko.kkubeurakko_be.global.oauth.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.OAuthProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NaverOAuthUnlinkClient implements OAuthUnlinkClient {

	private final RestTemplate oauthRestTemplate;

	@Value("${spring.security.oauth2.client.registration.naver.client-id}")
	private String clientId;

	@Value("${spring.security.oauth2.client.registration.naver.client-secret}")
	private String clientSecret;

	@Value("${spring.security.naver.unlink-url}")
	private String unlinkUrl;

	@Override
	public OAuthProvider supports() {
		return OAuthProvider.NAVER;
	}

	@Override
	public void unlink(String accessToken) {
		String url = unlinkUrl +
			"?grant_type=delete" +
			"&client_id=" + clientId +
			"&client_secret=" + clientSecret +
			"&access_token=" + accessToken;

		oauthRestTemplate.getForEntity(url, String.class);
	}
}
