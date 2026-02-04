package com.kkubeurakko.kkubeurakko_be.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ResetPasswordRequestDto(
	String resetToken,
	@NotBlank(message = "비밀번호는 공백일 수 없습니다.")
	@Pattern(
		regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,16}$",
		message = "비밀번호는 8~16자의 영문 대/소문자, 숫자, 특수문자를 모두 포함해야 합니다."
	)
	String newPassword
) {
}
