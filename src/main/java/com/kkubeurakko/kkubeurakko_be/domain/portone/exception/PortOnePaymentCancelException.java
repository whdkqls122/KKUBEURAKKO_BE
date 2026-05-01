package com.kkubeurakko.kkubeurakko_be.domain.portone.exception;

import com.kkubeurakko.kkubeurakko_be.domain.portone.entity.ErrorAction;
import com.kkubeurakko.kkubeurakko_be.domain.portone.entity.PortOneErrorType;

import lombok.Getter;

@Getter
public class PortOnePaymentCancelException extends RuntimeException {
	private final ErrorAction action;
	private final PortOneErrorType errorType;

	public PortOnePaymentCancelException(
		String message,
		ErrorAction action,
		PortOneErrorType errorType,
		Throwable cause
	) {
		super(message, cause);
		this.action = action;
		this.errorType = errorType;
	}
}
