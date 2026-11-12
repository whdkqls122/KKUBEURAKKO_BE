package com.kkubeurakko.kkubeurakko_be.global.task.service;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkubeurakko.kkubeurakko_be.domain.payments.repository.PaymentRepository;
import com.kkubeurakko.kkubeurakko_be.domain.portone.service.PortOneService;
import com.kkubeurakko.kkubeurakko_be.global.oauth.service.OAuthUnlinkService;
import com.kkubeurakko.kkubeurakko_be.global.task.dto.CashReceiptCancelPayload;
import com.kkubeurakko.kkubeurakko_be.global.task.dto.RefundPayload;
import com.kkubeurakko.kkubeurakko_be.global.task.dto.SocialUnlinkPayload;
import com.kkubeurakko.kkubeurakko_be.global.task.entity.FailedTask;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskProcessorImpl implements TaskProcessor {
	private final OAuthUnlinkService oAuthUnlinkService;
	private final PortOneService portOneService;
	private final PaymentRepository paymentRepository;
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

		if (isUsedByCommittedOrder(payload.paymentId())) {
			log.warn("다른 주문에 사용된 결제라 결제 취소를 건너뜀 (paymentId={})", payload.paymentId());
			return;
		}
		portOneService.retryCancelPortOnePayment(payload.paymentId());
	}

	private void processCashReceiptCancel(FailedTask task) throws JsonProcessingException {

		CashReceiptCancelPayload payload =
			objectMapper.readValue(task.getPayload(), CashReceiptCancelPayload.class);

		if (isUsedByCommittedOrder(payload.paymentId())) {
			log.warn("다른 주문에 사용된 결제라 현금영수증 취소를 건너뜀 (paymentId={})", payload.paymentId());
			return;
		}
		portOneService.retryCancelIssueCashReceipt(payload.paymentId());
	}

	// 실패한 요청의 결제 저장은 롤백됐으므로, 결제가 남아 있다면 다른 요청이 커밋한 주문의 결제
	private boolean isUsedByCommittedOrder(String paymentId) {
		return paymentRepository.existsByPaymentId(paymentId);
	}
}
