package com.kkubeurakko.kkubeurakko_be.global.task.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkubeurakko.kkubeurakko_be.global.task.dto.SocialUnlinkPayload;
import com.kkubeurakko.kkubeurakko_be.global.task.entity.FailedTask;
import com.kkubeurakko.kkubeurakko_be.global.task.entity.TaskStatus;
import com.kkubeurakko.kkubeurakko_be.global.task.entity.TaskType;
import com.kkubeurakko.kkubeurakko_be.global.task.repository.FailedTaskRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FailedTaskServiceImpl implements FailedTaskService {
	private final FailedTaskRepository failedTaskRepository;
	private final ObjectMapper objectMapper;

	@Override
	public void saveSocialUnlinkTask(
		String userNumber,
		String provider,
		String errorType,
		String errorMessage
	) throws JsonProcessingException {
		SocialUnlinkPayload socialUnlinkPayload = SocialUnlinkPayload.builder()
			.userNumber(userNumber)
			.provider(provider)
			.build();

		String payload = objectMapper.writeValueAsString(socialUnlinkPayload);

		FailedTask failedTask = FailedTask.builder()
			.taskType(TaskType.SOCIAL_UNLINK)
			.payload(payload)
			.retryCount(0)
			.taskStatus(TaskStatus.PENDING)
			.lastErrorType(errorType)
			.lastErrorMessage(errorMessage)
			.build();

		failedTaskRepository.save(failedTask);
	}
}
