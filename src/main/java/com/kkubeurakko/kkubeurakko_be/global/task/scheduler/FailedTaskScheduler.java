package com.kkubeurakko.kkubeurakko_be.global.task.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kkubeurakko.kkubeurakko_be.domain.portone.exception.PortOnePaymentCancelException;
import com.kkubeurakko.kkubeurakko_be.global.task.entity.FailedTask;
import com.kkubeurakko.kkubeurakko_be.global.task.service.FailedTaskService;
import com.kkubeurakko.kkubeurakko_be.global.task.service.TaskProcessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class FailedTaskScheduler {
	private final FailedTaskService failedTaskService;
	private final TaskProcessor taskProcessor;

	@Scheduled(fixedDelay = 60000)
	public void retryFailedTasks() {

		List<FailedTask> tasks = failedTaskService.findRetryableTasks();

		if (tasks.isEmpty()) {
			return;
		}

		for (FailedTask task : tasks) {
			try {
				taskProcessor.process(task);
				failedTaskService.markSuccess(task);

			} catch (PortOnePaymentCancelException e) {

				switch (e.getAction()) {
					case SUCCESS -> failedTaskService.markSuccess(task);
					case FAIL -> task.markFailed();
					case RETRY -> failedTaskService.markFailure(task, e);
				}

			} catch (Exception e) {
				failedTaskService.markFailure(task, e);
			}
		}
	}
}
