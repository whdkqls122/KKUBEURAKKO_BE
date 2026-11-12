package com.kkubeurakko.kkubeurakko_be.global.task.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

	/**
	 * 작업마다 외부 API는 트랜잭션 밖에서 호출하고, 작업 상태 저장만 짧은 트랜잭션으로 처리
	 * 한 작업의 지연·실패가 다른 작업의 DB 커넥션 점유로 번지지 않도록 전체를 하나의 트랜잭션으로 묶지 않음
	 */
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
					case FAIL -> failedTaskService.markFailed(task);
					case RETRY -> failedTaskService.markFailure(task, e);
				}

			} catch (Exception e) {
				failedTaskService.markFailure(task, e);
			}
		}
	}
}
