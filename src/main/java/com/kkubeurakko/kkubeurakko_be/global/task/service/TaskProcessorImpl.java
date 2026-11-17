package com.kkubeurakko.kkubeurakko_be.global.task.service;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkubeurakko.kkubeurakko_be.domain.payments.entity.PaymentAttemptStatus;
import com.kkubeurakko.kkubeurakko_be.domain.payments.repository.PaymentRepository;
import com.kkubeurakko.kkubeurakko_be.domain.payments.service.PaymentAttemptService;
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
	private final PaymentAttemptService paymentAttemptService;
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

		if (!canCancel(payload.paymentId())) {
			log.warn("취소 요청 상태가 아니거나 다른 주문에 사용된 결제라 결제 취소를 건너뜀 (paymentId={})", payload.paymentId());
			return;
		}
		portOneService.retryCancelPortOnePayment(payload.paymentId());
	}

	private void processCashReceiptCancel(FailedTask task) throws JsonProcessingException {

		CashReceiptCancelPayload payload =
			objectMapper.readValue(task.getPayload(), CashReceiptCancelPayload.class);

		if (!canCancel(payload.paymentId())) {
			log.warn("취소 요청 상태가 아니거나 다른 주문에 사용된 결제라 현금영수증 취소를 건너뜀 (paymentId={})", payload.paymentId());
			return;
		}
		portOneService.retryCancelIssueCashReceipt(payload.paymentId());
	}

	/**
	 * 결제 처리 기록이 취소 요청 상태일 때만 취소
	 * 주문 완료와 취소 요청은 조건부 상태 전이라 둘 중 하나만 성공하므로, 이 상태면 같은 결제로 주문이 커밋
	 * 처리 기록이 없는 이전 작업은 커밋된 주문이 있는지로 판단
	 */
	private boolean canCancel(String paymentId) {
		return paymentAttemptService.findStatus(paymentId)
			.map(status -> status == PaymentAttemptStatus.CANCEL_REQUESTED)
			.orElseGet(() -> !paymentRepository.existsByPaymentId(paymentId));
	}
}
