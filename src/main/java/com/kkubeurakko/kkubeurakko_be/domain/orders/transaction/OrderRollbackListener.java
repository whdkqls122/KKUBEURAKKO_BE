package com.kkubeurakko.kkubeurakko_be.domain.orders.transaction;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kkubeurakko.kkubeurakko_be.domain.orders.exception.OrderCreateException;
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

	@TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
	public void handleRollback(OrderCreateException e) throws JsonProcessingException {
		String paymentId = e.getPaymentId();
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
}
