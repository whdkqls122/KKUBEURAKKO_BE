package com.kkubeurakko.kkubeurakko_be.domain.users.exception;

public class InProgressOrderException extends RuntimeException {
	public InProgressOrderException(String message) {
		super(message);
	}

}
