package com.kkubeurakko.kkubeurakko_be.global.security.exception;

public class InvalidAESKeyFormatException extends RuntimeException {
	public InvalidAESKeyFormatException(String message, Throwable cause) {
		super(message, cause);
	}
}
