package com.kkubeurakko.kkubeurakko_be.global.oauth.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.OAuthProvider;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.token.OAuthTokenResponse;
import com.kkubeurakko.kkubeurakko_be.global.oauth.refresher.OAuthTokenRefresher;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OAuth2TokenService {
	private final RedisTemplate<String, String> redisTemplate;
	private final Map<OAuthProvider, OAuthTokenRefresher> refresherMap;
	private static final String AT_KEY = "OAUTH:AT:";
	private static final String RT_KEY = "OAUTH:RT:";
	private static final String PROVIDER_KEY = "OAUTH:PROVIDER:";

	public OAuth2TokenService(
		RedisTemplate<String, String> redisTemplate,
		List<OAuthTokenRefresher> refreshers
	) {
		this.redisTemplate = redisTemplate;
		this.refresherMap = refreshers.stream()
			.collect(Collectors.toMap(
				OAuthTokenRefresher::supports,
				Function.identity()
			));
	}

	public void save
		(
			String userNumber,
			String provider,
			String accessToken,
			String refreshToken,
			long expiresInSeconds
		) {
		redisTemplate.opsForValue()
			.set(AT_KEY + userNumber, accessToken, expiresInSeconds, TimeUnit.SECONDS);

		if (refreshToken != null) {
			redisTemplate.opsForValue()
				.set(RT_KEY + userNumber, refreshToken);
		}

		redisTemplate.opsForValue()
			.set(PROVIDER_KEY + userNumber, provider);
	}

	public String getOAuthAccessToken(String userNumber) {
		return redisTemplate.opsForValue().get(AT_KEY + userNumber);
	}

	public String getOAuthRefreshToken(String userNumber) {
		return redisTemplate.opsForValue().get(RT_KEY + userNumber);
	}

	public String getProvider(String userNumber) {
		return redisTemplate.opsForValue().get(PROVIDER_KEY + userNumber);
	}

	public boolean isAccessTokenValid(String userNumber) {
		Long ttl = redisTemplate.getExpire(AT_KEY + userNumber, TimeUnit.SECONDS);
		return ttl != null && ttl > 0;
	}

	public String getValidAccessToken(String userNumber) {
		if (isAccessTokenValid(userNumber)) {
			return getOAuthAccessToken(userNumber);
		}

		log.info("AccessToken 만료 → refresh 시도 user={}", userNumber);
		return refreshAccessToken(userNumber);
	}

	public void deleteAll(String userNumber) {
		redisTemplate.delete(AT_KEY + userNumber);
		redisTemplate.delete(RT_KEY + userNumber);
		redisTemplate.delete(PROVIDER_KEY + userNumber);
	}

	private String refreshAccessToken(String userNumber) {
		String provider = getProvider(userNumber);
		String refreshToken = getOAuthRefreshToken(userNumber);

		if (refreshToken == null) {
			throw new IllegalStateException("RefreshToken 없음");
		}

		OAuthProvider oauthProvider =
			OAuthProvider.valueOf(provider.toUpperCase());

		OAuthTokenRefresher refresher =
			refresherMap.get(oauthProvider);

		if (refresher == null) {
			throw new IllegalStateException("지원하지 않는 OAuth Provider");
		}

		OAuthTokenResponse response =
			refresher.refresh(refreshToken);

		save(
			userNumber,
			provider,
			response.accessToken(),
			response.refreshToken(),
			response.expiresInSeconds()
		);

		return response.accessToken();
	}

}
