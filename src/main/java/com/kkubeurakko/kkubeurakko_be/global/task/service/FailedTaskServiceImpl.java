package com.kkubeurakko.kkubeurakko_be.global.task.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkubeurakko.kkubeurakko_be.global.task.dto.CashReceiptCancelPayload;
import com.kkubeurakko.kkubeurakko_be.global.task.dto.RefundPayload;
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

	@Override
	public void saveRefundPaymentTask(String paymentId, String errorType, String errorMessage)
		throws JsonProcessingException {
		RefundPayload refundPayload = RefundPayload.builder()
			.paymentId(paymentId)
			.build();

		String payload = objectMapper.writeValueAsString(refundPayload);

		FailedTask failedTask = FailedTask.builder()
			.taskType(TaskType.REFUND_PAYMENT)
			.payload(payload)
			.retryCount(0)
			.taskStatus(TaskStatus.PENDING)
			.lastErrorType(errorType)
			.lastErrorMessage(errorMessage)
			.build();

		failedTaskRepository.save(failedTask);
	}

	@Override
	public void saveCashReceiptCancelTask(String paymentId, String errorType, String errorMessage)
		throws JsonProcessingException {
		CashReceiptCancelPayload cashReceiptCancelPayload = CashReceiptCancelPayload.builder()
			.paymentId(paymentId)
			.build();

		String payload = objectMapper.writeValueAsString(cashReceiptCancelPayload);

		FailedTask failedTask = FailedTask.builder()
			.taskType(TaskType.CANCEL_CASH_RECEIPT)
			.payload(payload)
			.retryCount(0)
			.taskStatus(TaskStatus.PENDING)
			.lastErrorType(errorType)
			.lastErrorMessage(errorMessage)
			.build();

		failedTaskRepository.save(failedTask);
	}

	@Override
	@Transactional
	public void markSuccess(FailedTask failedTask) {
		failedTask.markSuccess();
	}

	@Override
	@Transactional
	public void markFailure(FailedTask failedTask, Exception e) {
		failedTask.increaseRetryCount();
		failedTask.updateError(e.getMessage(), e.getClass().getSimpleName());

		if (failedTask.getRetryCount() > 5) {
			failedTask.markFailed();
		}
	}

	@Override
	public List<FailedTask> findRetryableTasks() {
		return failedTaskRepository.findFailedTaskByTaskStatus(TaskStatus.PENDING);
	}
}
