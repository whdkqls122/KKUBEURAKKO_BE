package com.kkubeurakko.kkubeurakko_be.domain.orders.exception;

import lombok.Getter;

@Getter
public class OrderCreateException extends RuntimeException {
	private final String paymentId;

	public OrderCreateException(String message, String paymentId, Throwable cause) {
		super(message);
		this.paymentId = paymentId;
	}
}
