package com.kkubeurakko.kkubeurakko_be.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CheckDuplicateDto(
	@NotBlank(message = "아이디는 공백일 수 없습니다.")
	@Pattern(
		regexp = "^[a-z0-9_-]{5,20}$",
		message = "아이디는 5~20자의 영문 소문자, 숫자, 특수기호(_,-)만 사용 가능합니다."
	)
	String id
) {
}
