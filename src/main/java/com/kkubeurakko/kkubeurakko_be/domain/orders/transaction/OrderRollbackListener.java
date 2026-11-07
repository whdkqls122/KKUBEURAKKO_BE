package com.kkubeurakko.kkubeurakko_be.domain.orders.transaction;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kkubeurakko.kkubeurakko_be.domain.orders.exception.OrderCreateException;
import com.kkubeurakko.kkubeurakko_be.domain.payments.repository.PaymentRepository;
import com.kkubeurakko.kkubeurakko_be.domain.portone.service.PortOneService;
import com.kkubeurakko.kkubeurakko_be.global.task.service.FailedTaskService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderRollbackListener {

	private final PortOneService portOneService;
	private final FailedTaskService failedTaskService;
	private final PaymentRepository paymentRepository;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
	public void handleRollback(OrderCreateException e) throws JsonProcessingException {
		String paymentId = e.getPaymentId();

		// 다른 요청이 같은 결제로 주문을 커밋했다면 그 주문의 결제이므로 취소하지 않음
		if (isUsedByCommittedOrder(paymentId)) {
			log.warn("이미 다른 주문에 사용된 결제라 취소하지 않음 (paymentId={})", paymentId);
			return;
		}

		try {
			portOneService.cancelPortOnePayment(paymentId);
			log.info("자동 환불 성공 (paymentId={})", e.getPaymentId());
		} catch (Exception ex) {
			failedTaskService.saveRefundPaymentTask(paymentId, ex.getClass().getSimpleName(), ex.getMessage());
			log.error(
				"자동 환불 실패 (paymentId={}) - 반드시 수동 확인 필요",
				e.getPaymentId(),
				ex
			);
		}
		if (e.isCashReceipt()) {
			try {
				portOneService.cancelIssueCashReceipt(paymentId);
				log.info("현금 영수증 취소 성공 (paymentId={})", e.getPaymentId());
			} catch (Exception ex) {
				failedTaskService.saveCashReceiptCancelTask(paymentId, ex.getClass().getSimpleName(), ex.getMessage());
				log.error(
					"현금영수증 취소 실패 (paymentId={}) - 반드시 수동 확인 필요",
					e.getPaymentId(),
					ex
				);
			}
		}
	}

	private boolean isUsedByCommittedOrder(String paymentId) {
		try {
			return paymentRepository.existsByPaymentId(paymentId);
		} catch (RuntimeException ex) {
			// 확인할 수 없으면 기존처럼 취소를 진행해 결제가 남지 않도록 함
			log.error("결제 사용 여부 확인 실패 (paymentId={}) - 취소 진행", paymentId, ex);
			return false;
		}
	}
}
