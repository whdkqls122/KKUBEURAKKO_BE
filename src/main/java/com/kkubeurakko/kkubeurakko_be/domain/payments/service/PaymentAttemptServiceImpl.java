package com.kkubeurakko.kkubeurakko_be.domain.payments.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kkubeurakko.kkubeurakko_be.domain.payments.entity.PaymentAttempt;
import com.kkubeurakko.kkubeurakko_be.domain.payments.entity.PaymentAttemptStatus;
import com.kkubeurakko.kkubeurakko_be.domain.payments.exception.InvalidPaymentException;
import com.kkubeurakko.kkubeurakko_be.domain.payments.repository.PaymentAttemptRepository;
import com.kkubeurakko.kkubeurakko_be.global.task.service.FailedTaskService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentAttemptServiceImpl implements PaymentAttemptService {
	private final PaymentAttemptRepository paymentAttemptRepository;
	private final FailedTaskService failedTaskService;

	/**
	 * 주문 처리 전에 별도 트랜잭션으로 바로 커밋
	 * paymentId unique 제약으로 같은 결제의 동시 요청 중 하나만 통과
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void start(String paymentId, boolean cashReceipt) {
		try {
			paymentAttemptRepository.saveAndFlush(PaymentAttempt.start(paymentId, cashReceipt));
		} catch (DataIntegrityViolationException e) {
			throw new InvalidPaymentException("이미 처리 중이거나 사용된 결제입니다.");
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void discard(String paymentId) {
		paymentAttemptRepository.deleteByPaymentIdAndStatus(paymentId, PaymentAttemptStatus.PROCESSING);
	}

	/**
	 * 주문 저장과 같은 트랜잭션에서 실행
	 * 이미 취소 요청된 결제면 예외로 주문 트랜잭션을 롤백
	 */
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void complete(String paymentId) {
		int updated = paymentAttemptRepository.changeStatus(
			paymentId, PaymentAttemptStatus.PROCESSING, PaymentAttemptStatus.COMPLETED);
		if (updated == 0) {
			throw new IllegalStateException("결제 처리 상태가 변경되어 주문을 완료할 수 없습니다.");
		}
	}

	/**
	 * 상태 전이와 취소 작업 등록을 한 트랜잭션으로 처리한다(Outbox).
	 * 롤백 이후 리스너에서도 호출하므로 항상 새 트랜잭션에서 실행한다.
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean requestCancel(String paymentId, String reasonType, String reasonMessage) {
		int updated = paymentAttemptRepository.changeStatus(
			paymentId, PaymentAttemptStatus.PROCESSING, PaymentAttemptStatus.CANCEL_REQUESTED);
		if (updated == 0) {
			log.info("처리 중인 결제가 아니라 취소 요청하지 않음 (paymentId={})", paymentId);
			return false;
		}

		PaymentAttempt attempt = paymentAttemptRepository.findByPaymentId(paymentId)
			.orElseThrow(() -> new IllegalStateException("결제 처리 기록이 없습니다. paymentId=" + paymentId));
		try {
			failedTaskService.saveRefundPaymentTask(paymentId, reasonType, reasonMessage);
			if (attempt.isCashReceipt()) {
				failedTaskService.saveCashReceiptCancelTask(paymentId, reasonType, reasonMessage);
			}
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("결제 취소 작업을 만들 수 없습니다. paymentId=" + paymentId, e);
		}
		return true;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<PaymentAttemptStatus> findStatus(String paymentId) {
		return paymentAttemptRepository.findByPaymentId(paymentId)
			.map(PaymentAttempt::getStatus);
	}

	@Override
	@Transactional(readOnly = true)
	public List<String> findProcessingPaymentIdsStartedBefore(LocalDateTime time) {
		return paymentAttemptRepository.findByStatusAndCreateAtBefore(PaymentAttemptStatus.PROCESSING, time)
			.stream()
			.map(PaymentAttempt::getPaymentId)
			.toList();
	}
}