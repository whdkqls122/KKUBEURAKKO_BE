package com.kkubeurakko.kkubeurakko_be.global.task.service;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkubeurakko.kkubeurakko_be.domain.payments.entity.PaymentAttemptStatus;
import com.kkubeurakko.kkubeurakko_be.domain.payments.exception.InvalidPaymentException;
import com.kkubeurakko.kkubeurakko_be.domain.payments.repository.PaymentRepository;
import com.kkubeurakko.kkubeurakko_be.domain.payments.service.PaymentAttemptService;
import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.PortOnePaymentResponseDto;
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
		String paymentId = payload.paymentId();

		if (!canCancel(paymentId)) {
			log.warn("취소 요청 상태가 아니거나 다른 주문에 사용된 결제라 결제 취소를 건너뜀 (paymentId={})", paymentId);
			return;
		}
		Optional<PortOnePaymentResponseDto> payment = findOwnPayment(paymentId);
		if (payment.isEmpty() || !payment.get().isPaid()) {
			log.warn("취소할 수 있는 결제가 아니라 결제 취소를 건너뜀 (paymentId={}, status={})",
				paymentId, payment.map(PortOnePaymentResponseDto::status).orElse(null));
			return;
		}
		portOneService.retryCancelPortOnePayment(paymentId);
	}

	private void processCashReceiptCancel(FailedTask task) throws JsonProcessingException {

		CashReceiptCancelPayload payload =
			objectMapper.readValue(task.getPayload(), CashReceiptCancelPayload.class);
		String paymentId = payload.paymentId();

		if (!canCancel(paymentId)) {
			log.warn("취소 요청 상태가 아니거나 다른 주문에 사용된 결제라 현금영수증 취소를 건너뜀 (paymentId={})", paymentId);
			return;
		}
		// 결제 취소가 먼저 처리돼 결제가 CANCELLED여도 현금영수증은 취소해야 하므로 상점만 확인
		if (findOwnPayment(paymentId).isEmpty()) {
			log.warn("우리 상점 결제가 아니라 현금영수증 취소를 건너뜀 (paymentId={})", paymentId);
			return;
		}
		portOneService.retryCancelIssueCashReceipt(paymentId);
	}

	/**
	 * 결제 처리 기록이 취소 요청 상태일 때만 취소
	 * 주문 완료와 취소 요청은 조건부 상태 전이라 둘 중 하나만 성공하므로, 이 상태면 같은 결제로 주문이 커밋될 수 없음
	 * 처리 기록이 없는 이전 작업은 커밋된 주문이 있는지로 판단
	 */
	private boolean canCancel(String paymentId) {
		return paymentAttemptService.findStatus(paymentId)
			.map(status -> status == PaymentAttemptStatus.CANCEL_REQUESTED)
			.orElseGet(() -> !paymentRepository.existsByPaymentId(paymentId));
	}

	/**
	 * 결제 처리 기록은 결제 확인 전에 남으므로, 취소 전에 PortOne에서 우리 상점의 결제인지 다시 확인
	 * 우리 상점 결제가 아니면 취소할 것이 없고, 조회 자체가 실패하면 예외를 그대로 던져 다음 주기에 재시도
	 */
	private Optional<PortOnePaymentResponseDto> findOwnPayment(String paymentId) {
		try {
			return Optional.of(portOneService.getOwnPayment(paymentId));
		} catch (InvalidPaymentException e) {
			log.warn("우리 상점 결제로 확인되지 않음 (paymentId={}, 사유={})", paymentId, e.getMessage());
			return Optional.empty();
		}
	}
}