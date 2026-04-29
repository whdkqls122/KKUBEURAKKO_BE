package com.kkubeurakko.kkubeurakko_be.global.task.service;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkubeurakko.kkubeurakko_be.global.oauth.service.OAuthUnlinkService;
import com.kkubeurakko.kkubeurakko_be.global.task.dto.SocialUnlinkPayload;
import com.kkubeurakko.kkubeurakko_be.global.task.entity.FailedTask;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TaskProcessorImpl implements TaskProcessor {
	private final OAuthUnlinkService oAuthUnlinkService;
	private final ObjectMapper objectMapper;

	@Override
	public void process(FailedTask failedTask) throws JsonProcessingException {
		switch (failedTask.getTaskType()) {
			case SOCIAL_UNLINK -> processSocialUnlink(failedTask);
			// case REFUND ->
		}
	}

	private void processSocialUnlink(FailedTask task) throws JsonProcessingException {

		SocialUnlinkPayload payload =
			objectMapper.readValue(task.getPayload(), SocialUnlinkPayload.class);

		oAuthUnlinkService.retryRevokeOAuthAccount(payload.userNumber());
	}
}
