package com.kkubeurakko.kkubeurakko_be.domain.coupons.exception;

public class DuplicatedCouponNumberException extends RuntimeException {
	public DuplicatedCouponNumberException(String message, Throwable cause) {
		super(message, cause);
	}

}
