package com.kkubeurakko.kkubeurakko_be.global.oauth.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kkubeurakko.kkubeurakko_be.global.oauth.client.OAuthUnlinkClient;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.OAuthProvider;
import com.kkubeurakko.kkubeurakko_be.global.task.service.FailedTaskService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OAuthUnlinkService {
	private final OAuth2TokenService tokenService;
	private final FailedTaskService failedTaskService;
	private final Map<OAuthProvider, OAuthUnlinkClient> unlinkClientMap;

	public OAuthUnlinkService(
		OAuth2TokenService tokenService,
		List<OAuthUnlinkClient> clients,
		FailedTaskService failedTaskService
	) {
		this.tokenService = tokenService;
		this.failedTaskService = failedTaskService;
		this.unlinkClientMap = clients.stream()
			.collect(Collectors.toMap(
				OAuthUnlinkClient::supports,
				Function.identity()
			));
	}

	public void revokeOAuthAccount(String userNumber) throws JsonProcessingException {
		String accessToken = tokenService.getValidAccessToken(userNumber);
		String provider = tokenService.getProvider(userNumber);

		if (accessToken == null || provider == null) {
			log.warn("OAuth 토큰 없음 userNumber={}", userNumber);
			return;
		}

		OAuthProvider oauthProvider =
			OAuthProvider.valueOf(provider.toUpperCase());

		OAuthUnlinkClient client = unlinkClientMap.get(oauthProvider);

		try {
			client.unlink(accessToken);
			tokenService.deleteAll(userNumber);
		} catch (Exception e) {
			log.error("OAuth unlink 실패 userNumber={}", userNumber, e);
			failedTaskService.saveSocialUnlinkTask(
				userNumber,
				provider,
				e.getMessage(),
				e.getClass().getSimpleName()
			);
		}
	}
}

