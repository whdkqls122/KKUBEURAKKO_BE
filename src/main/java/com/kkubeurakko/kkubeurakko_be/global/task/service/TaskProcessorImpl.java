package com.kkubeurakko.kkubeurakko_be.global.task.service;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkubeurakko.kkubeurakko_be.domain.portone.service.PortOneService;
import com.kkubeurakko.kkubeurakko_be.global.oauth.service.OAuthUnlinkService;
import com.kkubeurakko.kkubeurakko_be.global.task.dto.CashReceiptCancelPayload;
import com.kkubeurakko.kkubeurakko_be.global.task.dto.RefundPayload;
import com.kkubeurakko.kkubeurakko_be.global.task.dto.SocialUnlinkPayload;
import com.kkubeurakko.kkubeurakko_be.global.task.entity.FailedTask;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TaskProcessorImpl implements TaskProcessor {
	private final OAuthUnlinkService oAuthUnlinkService;
	private final PortOneService portOneService;
	private final ObjectMapper objectMapper;

	@Override
	public void process(FailedTask failedTask) throws JsonProcessingException {
		switch (failedTask.getTaskType()) {
			case SOCIAL_UNLINK -> processSocialUnlink(failedTask);
			case REFUND_PAYMENT -> processRefundPayment(failedTask);
			case CANCEL_CASH_RECEIPT -> processCashReceiptCancel(failedTask);
		}
	}

	private void processSocialUnlink(FailedTask task) throws JsonProcessingException {

		SocialUnlinkPayload payload =
			objectMapper.readValue(task.getPayload(), SocialUnlinkPayload.class);

		oAuthUnlinkService.retryRevokeOAuthAccount(payload.userNumber());
	}

	private void processRefundPayment(FailedTask task) throws JsonProcessingException {

		RefundPayload payload =
			objectMapper.readValue(task.getPayload(), RefundPayload.class);

		portOneService.retryCancelPortOnePayment(payload.paymentId());
	}

	private void processCashReceiptCancel(FailedTask task) throws JsonProcessingException {

		CashReceiptCancelPayload payload =
			objectMapper.readValue(task.getPayload(), CashReceiptCancelPayload.class);

		portOneService.retryCancelIssueCashReceipt(payload.paymentId());
	}
}
