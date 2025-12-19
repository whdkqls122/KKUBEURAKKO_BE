package com.kkubeurakko.kkubeurakko_be.domain.users.exception;

public class UserVerifyException extends RuntimeException {
	public UserVerifyException(String message, Throwable cause) {
		super(message, cause);
	}
}
