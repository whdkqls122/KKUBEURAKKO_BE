package com.kkubeurakko.kkubeurakko_be.global.security.exception;

public class AESEncryptException extends RuntimeException {
	public AESEncryptException(String message, Throwable cause) {
		super(message, cause);
	}
}
