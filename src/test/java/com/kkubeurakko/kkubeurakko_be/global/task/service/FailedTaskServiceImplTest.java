package com.kkubeurakko.kkubeurakko_be.global.task.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkubeurakko.kkubeurakko_be.global.task.entity.FailedTask;
import com.kkubeurakko.kkubeurakko_be.global.task.entity.TaskStatus;
import com.kkubeurakko.kkubeurakko_be.global.task.entity.TaskType;
import com.kkubeurakko.kkubeurakko_be.global.task.repository.FailedTaskRepository;

@ExtendWith(MockitoExtension.class)
class FailedTaskServiceImplTest {

	@Mock
	private FailedTaskRepository failedTaskRepository;
	@Mock
	private ObjectMapper objectMapper;
	@InjectMocks
	private FailedTaskServiceImpl failedTaskService;

	private FailedTask refundTask(int retryCount) {
		return FailedTask.builder()
			.taskType(TaskType.REFUND_PAYMENT)
			.payload("{\"paymentId\":\"paymentid\"}")
			.retryCount(retryCount)
			.taskStatus(TaskStatus.PENDING)
			.build();
	}

	@Test
	@DisplayName("성공 처리하면 상태를 SUCCESS로 바꾸고 저장한다")
	void markSuccess() {
		// given
		FailedTask task = refundTask(0);

		// when
		failedTaskService.markSuccess(task);

		// then
		assertThat(task.getTaskStatus()).isEqualTo(TaskStatus.SUCCESS);
		verify(failedTaskRepository).save(task);
	}

	@Test
	@DisplayName("재시도 실패 시 횟수와 오류를 기록하고 저장하며, 5회를 넘으면 FAILED로 바꾼다")
	void markFailure_overMaxRetry() {
		// given
		FailedTask task = refundTask(5);

		// when
		failedTaskService.markFailure(task, new RuntimeException("timeout"));

		// then
		assertThat(task.getRetryCount()).isEqualTo(6);
		assertThat(task.getLastErrorType()).isEqualTo("RuntimeException");
		assertThat(task.getTaskStatus()).isEqualTo(TaskStatus.FAILED);
		verify(failedTaskRepository).save(task);
	}

	@Test
	@DisplayName("실패 처리하면 상태를 FAILED로 바꾸고 저장한다")
	void markFailed() {
		// given
		FailedTask task = refundTask(0);

		// when
		failedTaskService.markFailed(task);

		// then
		assertThat(task.getTaskStatus()).isEqualTo(TaskStatus.FAILED);
		verify(failedTaskRepository).save(task);
	}
}