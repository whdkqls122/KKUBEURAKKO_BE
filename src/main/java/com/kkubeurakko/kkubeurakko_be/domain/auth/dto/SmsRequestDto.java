package com.kkubeurakko.kkubeurakko_be.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SmsRequestDto(
	@NotBlank(message = "휴대전화 번호를 입력해 주세요")
	@Pattern(regexp = "^(01[016789])-\\d{4}-\\d{4}$|^(0[3-9]\\d)-\\d{4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다.")
	@Size(max = 13, min = 13)
	String phoneNum
) {
}
