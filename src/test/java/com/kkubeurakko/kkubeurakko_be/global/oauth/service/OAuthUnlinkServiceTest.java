package com.kkubeurakko.kkubeurakko_be.global.oauth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kkubeurakko.kkubeurakko_be.global.oauth.client.OAuthUnlinkClient;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.OAuthProvider;
import com.kkubeurakko.kkubeurakko_be.global.task.service.FailedTaskService;

@ExtendWith(MockitoExtension.class)
class OAuthUnlinkServiceTest {

	@Mock
	OAuth2TokenService tokenService;
	@Mock
	OAuthUnlinkClient client;
	@Mock
	FailedTaskService failedTaskService;

	OAuthUnlinkService service;

	@BeforeEach
	void setUp() {
		List<OAuthUnlinkClient> clients = List.of(client);

		when(client.supports()).thenReturn(OAuthProvider.KAKAO);

		service = new OAuthUnlinkService(
			tokenService,
			clients,
			failedTaskService
		);
	}

	@Test
	@DisplayName("소셜 연결 해제 요청이 성공적으로 수행된다.")
	void unlink_success() throws JsonProcessingException {
		when(tokenService.getValidAccessToken("user1")).thenReturn("token");
		when(tokenService.getProvider("user1")).thenReturn("kakao");

		service.revokeOAuthAccount("user1");

		verify(client).unlink("token");
		verify(tokenService).deleteAll("user1");
		verify(failedTaskService, never()).saveSocialUnlinkTask(any(), any(), any(), any());
	}

	@Test
	@DisplayName("소셜 연결 해제 시 예외가 발생하면 실패 작업 엔티티 저장이 수행된다.")
	void unlink_fail_save_task() throws JsonProcessingException {
		when(tokenService.getValidAccessToken("user1")).thenReturn("token");
		when(tokenService.getProvider("user1")).thenReturn("kakao");

		doThrow(new RuntimeException("fail"))
			.when(client).unlink("token");

		service.revokeOAuthAccount("user1");

		verify(failedTaskService).saveSocialUnlinkTask(any(), any(), any(), any());
	}

	@Test
	@DisplayName("소셜 연결 해제 요청 재시도가 실패하면 예외가 던진다.")
	void retry_fail_throw_exception() {
		when(tokenService.getValidAccessToken("user1")).thenReturn("token");
		when(tokenService.getProvider("user1")).thenReturn("kakao");

		doThrow(new RuntimeException("fail"))
			.when(client).unlink("token");

		assertThrows(RuntimeException.class,
			() -> service.retryRevokeOAuthAccount("user1"));
	}
}