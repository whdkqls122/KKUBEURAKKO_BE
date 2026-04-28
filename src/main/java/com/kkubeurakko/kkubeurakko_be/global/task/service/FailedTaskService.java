package com.kkubeurakko.kkubeurakko_be.global.task.service;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface FailedTaskService {
	void saveSocialUnlinkTask(
		String userNumber,
		String provider,
		String errorType,
		String errorMessage
	) throws JsonProcessingException;
}
