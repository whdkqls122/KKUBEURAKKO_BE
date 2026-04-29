package com.kkubeurakko.kkubeurakko_be.global.task.service;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kkubeurakko.kkubeurakko_be.global.task.entity.FailedTask;

public interface FailedTaskService {
	void saveSocialUnlinkTask(
		String userNumber,
		String provider,
		String errorType,
		String errorMessage
	) throws JsonProcessingException;

	void markSuccess(FailedTask failedTask);

	void markFailure(FailedTask failedTask, Exception e);

	List<FailedTask> findRetryableTasks();
}
