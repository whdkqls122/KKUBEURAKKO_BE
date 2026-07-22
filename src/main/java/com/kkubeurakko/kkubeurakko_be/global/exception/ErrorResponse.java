package com.kkubeurakko.kkubeurakko_be.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
	private int status;
	private String message;
}
