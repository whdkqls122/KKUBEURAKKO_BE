package com.kkubeurakko.kkubeurakko_be.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record LoginRequestDto(
	@NotBlank(message = "ID는 공백일 수 없습니다.")
	@Size(max = 20, message = "ID는 20글자 이하여야 합니다.")
	String adminId,
	@NotBlank(message = "비밀번호는 공백일 수 없습니다.")
	@Size(min = 8, max = 20, message = "비밀번호는 8~20자리여야 합니다.")
	@Pattern(
		regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&]).{8,20}$",
		message = "비밀번호는 영문자, 숫자, 특수문자를 포함해야 합니다."
	)
	String password
) {}
