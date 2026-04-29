package com.kkubeurakko.kkubeurakko_be.global.task.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kkubeurakko.kkubeurakko_be.global.task.entity.FailedTask;

public interface TaskProcessor {
	void process(FailedTask failedTask) throws JsonProcessingException;
}
