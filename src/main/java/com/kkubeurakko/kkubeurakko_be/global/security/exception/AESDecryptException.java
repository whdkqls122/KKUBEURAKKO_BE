package com.kkubeurakko.kkubeurakko_be.global.security.exception;

public class AESDecryptException extends RuntimeException {
	public AESDecryptException(String message, Throwable cause) {
		super(message, cause);
	}
}
