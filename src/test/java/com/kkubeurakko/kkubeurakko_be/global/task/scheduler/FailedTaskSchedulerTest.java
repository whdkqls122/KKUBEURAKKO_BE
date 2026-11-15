package com.kkubeurakko.kkubeurakko_be.global.task.scheduler;

import static org.mockito.BDDMockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kkubeurakko.kkubeurakko_be.domain.portone.entity.ErrorAction;
import com.kkubeurakko.kkubeurakko_be.domain.portone.entity.PortOneErrorType;
import com.kkubeurakko.kkubeurakko_be.domain.portone.exception.PortOnePaymentCancelException;
import com.kkubeurakko.kkubeurakko_be.global.task.entity.FailedTask;
import com.kkubeurakko.kkubeurakko_be.global.task.entity.TaskStatus;
import com.kkubeurakko.kkubeurakko_be.global.task.entity.TaskType;
import com.kkubeurakko.kkubeurakko_be.global.task.service.FailedTaskService;
import com.kkubeurakko.kkubeurakko_be.global.task.service.TaskProcessor;

@ExtendWith(MockitoExtension.class)
class FailedTaskSchedulerTest {

	@Mock
	private FailedTaskService failedTaskService;
	@Mock
	private TaskProcessor taskProcessor;
	@InjectMocks
	private FailedTaskScheduler scheduler;

	private FailedTask refundTask() {
		return FailedTask.builder()
			.taskType(TaskType.REFUND_PAYMENT)
			.payload("{\"paymentId\":\"paymentid\"}")
			.retryCount(0)
			.taskStatus(TaskStatus.PENDING)
			.build();
	}

	private PortOnePaymentCancelException cancelException(ErrorAction action, PortOneErrorType type) {
		return new PortOnePaymentCancelException("PortOne 취소 실패", action, type, null);
	}

	@Test
	@DisplayName("작업 처리에 성공하면 성공으로 기록한다")
	void retryFailedTasks_success() throws Exception {
		// given
		FailedTask task = refundTask();
		given(failedTaskService.findRetryableTasks()).willReturn(List.of(task));

		// when
		scheduler.retryFailedTasks();

		// then
		verify(taskProcessor).process(task);
		verify(failedTaskService).markSuccess(task);
	}

	@Test
	@DisplayName("이미 취소된 결제처럼 결과가 같은 오류면 성공으로 기록한다")
	void retryFailedTasks_alreadyCancelled() throws Exception {
		// given
		FailedTask task = refundTask();
		given(failedTaskService.findRetryableTasks()).willReturn(List.of(task));
		willThrow(cancelException(ErrorAction.SUCCESS, PortOneErrorType.PAYMENT_ALREADY_CANCELLED))
			.given(taskProcessor).process(task);

		// when
		scheduler.retryFailedTasks();

		// then
		verify(failedTaskService).markSuccess(task);
	}

	@Test
	@DisplayName("재시도해도 결과가 같은 오류면 바로 실패로 기록한다")
	void retryFailedTasks_fail() throws Exception {
		// given
		FailedTask task = refundTask();
		given(failedTaskService.findRetryableTasks()).willReturn(List.of(task));
		willThrow(cancelException(ErrorAction.FAIL, PortOneErrorType.INVALID_REQUEST))
			.given(taskProcessor).process(task);

		// when
		scheduler.retryFailedTasks();

		// then
		verify(failedTaskService).markFailed(task);
		verify(failedTaskService, never()).markSuccess(any());
	}

	@Test
	@DisplayName("재시도가 필요한 오류면 재시도 횟수를 늘린다")
	void retryFailedTasks_retry() throws Exception {
		// given
		FailedTask task = refundTask();
		given(failedTaskService.findRetryableTasks()).willReturn(List.of(task));
		willThrow(cancelException(ErrorAction.RETRY, PortOneErrorType.PG_PROVIDER))
			.given(taskProcessor).process(task);

		// when
		scheduler.retryFailedTasks();

		// then
		verify(failedTaskService).markFailure(eq(task), any(PortOnePaymentCancelException.class));
	}

	@Test
	@DisplayName("한 작업이 실패해도 다음 작업을 계속 처리한다")
	void retryFailedTasks_continueAfterFailure() throws Exception {
		// given
		FailedTask failing = refundTask();
		FailedTask next = refundTask();
		given(failedTaskService.findRetryableTasks()).willReturn(List.of(failing, next));
		willThrow(new RuntimeException("timeout")).given(taskProcessor).process(failing);

		// when
		scheduler.retryFailedTasks();

		// then
		verify(failedTaskService).markFailure(eq(failing), any(RuntimeException.class));
		verify(failedTaskService).markSuccess(next);
	}
}