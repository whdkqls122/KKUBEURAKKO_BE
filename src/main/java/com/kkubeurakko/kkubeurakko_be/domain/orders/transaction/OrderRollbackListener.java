package com.kkubeurakko.kkubeurakko_be.domain.orders.transaction;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.kkubeurakko.kkubeurakko_be.domain.orders.exception.OrderCreateException;
import com.kkubeurakko.kkubeurakko_be.domain.portone.service.PortOneService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderRollbackListener {

	private final PortOneService portOneService;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
	public void handleRollback(OrderCreateException e) {
		try {
			portOneService.cancelPortOnePayment(e.getPaymentId());
			portOneService.cancelIssueCashReceipt(e.getPaymentId());
			log.info("자동 환불 및 현금 영수증 취소 성공 (paymentId={})", e.getPaymentId());
		} catch (Exception ex) {
			log.error(
				"자동 환불 실패 (paymentId={}) - 반드시 수동 확인 필요",
				e.getPaymentId(),
				ex
			);
		}
	}
}
