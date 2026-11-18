package com.kkubeurakko.kkubeurakko_be.domain.orders.transaction;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.kkubeurakko.kkubeurakko_be.domain.orders.exception.OrderCreateException;
import com.kkubeurakko.kkubeurakko_be.domain.payments.service.PaymentAttemptService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderRollbackListener {

	private final PaymentAttemptService paymentAttemptService;

	/**
	 * 주문 생성이 롤백되면 결제 처리 기록을 취소 요청 상태로 바꾸고 취소 작업을 등록
	 * 여기서 실패해도 기록이 처리 중으로 남아 방치 감지 스케줄러가 다시 취소를 요청
	 */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
	public void handleRollback(OrderCreateException e) {
		String paymentId = e.getPaymentId();
		Throwable cause = e.getCause() != null ? e.getCause() : e;

		try {
			if (paymentAttemptService.requestCancel(paymentId, cause.getClass().getSimpleName(), cause.getMessage())) {
				log.info("결제 취소 요청 (paymentId={}, 사유={})", paymentId, cause.getClass().getSimpleName());
			}
		} catch (Exception ex) {
			log.error("결제 취소 요청 실패, 방치 감지 스케줄러가 다시 처리 (paymentId={})", paymentId, ex);
		}
	}
}