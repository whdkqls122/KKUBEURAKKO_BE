package com.kkubeurakko.kkubeurakko_be.domain.orders.exception;

import lombok.Getter;

@Getter
public class OrderCreateException extends RuntimeException {
	private final String paymentId;
	private final boolean cashReceipt;

	public OrderCreateException(String message, String paymentId, boolean cashReceipt, Throwable cause) {
		super(message);
		this.paymentId = paymentId;
		this.cashReceipt = cashReceipt;
	}
}
