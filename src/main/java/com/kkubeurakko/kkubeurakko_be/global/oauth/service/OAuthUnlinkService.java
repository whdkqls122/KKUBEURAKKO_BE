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

	//소셜 연결 해제
	public void revokeOAuthAccount(String userNumber) throws JsonProcessingException {
		try {
			doUnlink(userNumber);
		} catch (Exception e) {
			log.error("OAuth unlink 실패 userNumber={}", userNumber, e);

			failedTaskService.saveSocialUnlinkTask(
				userNumber,
				tokenService.getProvider(userNumber),
				e.getClass().getSimpleName(),
				e.getMessage()
			);
		}
	}

	/*
	 * 재시도 소셜 연결 해제
	 * 위 revokeOAuthAccount 는 호출부 쪽에서 예외를 받기 때문에 상관없지만
	 * 재시도 시에는 소셜 연결 요청만 수행하기 때문에 예외를 던지는 곳이 없음
	 * 따라서 재시도 요청 로직을 분리
	 */
	public void retryRevokeOAuthAccount(String userNumber) {

		try {
			doUnlink(userNumber);

		} catch (Exception e) {
			log.error("OAuth unlink 재시도 실패 userNumber={}", userNumber, e);
			throw e;
		}
	}

	private void doUnlink(String userNumber) {
		String accessToken = tokenService.getValidAccessToken(userNumber);
		String provider = tokenService.getProvider(userNumber);

		if (accessToken == null || provider == null) {
			log.warn("OAuth 토큰 없음 userNumber={}", userNumber);
			return;
		}

		OAuthProvider oauthProvider =
			OAuthProvider.valueOf(provider.toUpperCase());

		OAuthUnlinkClient client = unlinkClientMap.get(oauthProvider);

		client.unlink(accessToken);

		tokenService.deleteAll(userNumber);
	}
}

