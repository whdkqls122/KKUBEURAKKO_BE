package com.kkubeurakko.kkubeurakko_be.domain.auth.dto;

public record VerifyForResetPasswordRequestDto(
	String verificationSessionId,
	String identityVerificationId
) {
}
