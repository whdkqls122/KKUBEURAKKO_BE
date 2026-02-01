package com.kkubeurakko.kkubeurakko_be.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignupRequestDto(
	@NotBlank(message = "회원 아이디는 공백일 수 없습니다.")
	@Pattern(
		regexp = "^[a-z0-9_-]{5,20}$",
		message = "아이디는 5~20자의 영문 소문자, 숫자, 특수기호(_,-)만 사용 가능합니다."
	)
	String id,

	@NotBlank(message = "비밀번호는 공백일 수 없습니다.")
	@Pattern(
		regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,16}$",
		message = "비밀번호는 8~16자의 영문 대/소문자, 숫자, 특수문자를 모두 포함해야 합니다."
	)
	String password,

	@NotBlank(message = "닉네임은 공백일 수 없습니다.")
	@Pattern(
		regexp = "^[가-힣a-zA-Z0-9]{2,10}$",
		message = "닉네임은 2~10자의 한글, 영문, 숫자만 입력 가능합니다."
	)
	String nickname
) {
}
