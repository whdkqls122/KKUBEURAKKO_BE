package com.kkubeurakko.kkubeurakko_be.domain.payments.exception;

/**
 * 주문에 사용할 수 없는 결제라서 거절하는 경우.
 * 완료되지 않았거나 다른 상점의 결제, 이미 사용된 결제처럼 자동 환불 대상이 아닐 때 사용
 */
public class InvalidPaymentException extends IllegalArgumentException {
	public InvalidPaymentException(String message) {
		super(message);
	}
}