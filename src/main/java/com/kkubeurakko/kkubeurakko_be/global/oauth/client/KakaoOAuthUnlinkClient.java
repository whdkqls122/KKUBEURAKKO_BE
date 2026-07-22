package com.kkubeurakko.kkubeurakko_be.global.oauth.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.OAuthProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoOAuthUnlinkClient implements OAuthUnlinkClient {
	@Value("${spring.security.kakao.unlink-url}")
	private String unlinkUrl;
	private final RestTemplate oauthRestTemplate;

	@Override
	public OAuthProvider supports() {
		return OAuthProvider.KAKAO;
	}

	@Override
	public void unlink(String accessToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);

		HttpEntity<?> request = new HttpEntity<>(headers);
		oauthRestTemplate.postForEntity(unlinkUrl, request, String.class);
	}
}
