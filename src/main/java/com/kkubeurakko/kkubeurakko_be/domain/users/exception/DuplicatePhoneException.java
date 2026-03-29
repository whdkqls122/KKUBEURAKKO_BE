package com.kkubeurakko.kkubeurakko_be.domain.users.exception;

public class DuplicatePhoneException extends RuntimeException {
	public DuplicatePhoneException(String message) {
		super(message);
	}
}