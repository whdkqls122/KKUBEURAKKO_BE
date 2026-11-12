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

	/**
	 * 주문 생성이 롤백되면 결제 및 현금영수증 취소 작업을 등록
	 * 실제 취소는 스케줄러가 실행 직전에 결제 사용 여부를 다시 확인한 뒤 처리
	 * */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
	public void handleRollback(OrderCreateException e) {
		String paymentId = e.getPaymentId();
		Throwable cause = e.getCause() != null ? e.getCause() : e;
		String errorType = cause.getClass().getSimpleName();
		String errorMessage = cause.getMessage();

		registerRefundTask(paymentId, errorType, errorMessage);
		if (e.isCashReceipt()) {
			registerCashReceiptCancelTask(paymentId, errorType, errorMessage);
		}
	}

	private void registerRefundTask(String paymentId, String errorType, String errorMessage) {
		try {
			failedTaskService.saveRefundPaymentTask(paymentId, errorType, errorMessage);
			log.info("결제 취소 작업 등록 (paymentId={}, 사유={})", paymentId, errorType);
		} catch (Exception ex) {
			// 작업을 남기지 못하면 결제가 방치되므로 최후 수단으로 즉시 취소를 시도
			log.error("결제 취소 작업 등록 실패, 즉시 취소 시도 (paymentId={})", paymentId, ex);
			try {
				portOneService.cancelPortOnePayment(paymentId);
				log.info("결제 즉시 취소 성공 (paymentId={})", paymentId);
			} catch (Exception cancelEx) {
				log.error("결제 즉시 취소 실패 (paymentId={}) - 반드시 수동 확인 필요", paymentId, cancelEx);
			}
		}
	}

	private void registerCashReceiptCancelTask(String paymentId, String errorType, String errorMessage) {
		try {
			failedTaskService.saveCashReceiptCancelTask(paymentId, errorType, errorMessage);
			log.info("현금영수증 취소 작업 등록 (paymentId={}, 사유={})", paymentId, errorType);
		} catch (Exception ex) {
			log.error("현금영수증 취소 작업 등록 실패, 즉시 취소 시도 (paymentId={})", paymentId, ex);
			try {
				portOneService.cancelIssueCashReceipt(paymentId);
				log.info("현금영수증 즉시 취소 성공 (paymentId={})", paymentId);
			} catch (Exception cancelEx) {
				log.error("현금영수증 즉시 취소 실패 (paymentId={}) - 반드시 수동 확인 필요", paymentId, cancelEx);
			}
		}
	}
}
