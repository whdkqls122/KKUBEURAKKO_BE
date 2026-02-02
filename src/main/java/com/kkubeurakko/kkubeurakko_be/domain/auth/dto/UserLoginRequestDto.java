package com.kkubeurakko.kkubeurakko_be.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UserLoginRequestDto(
	@NotBlank(message = "아이디는 공백일 수 없습니다.")
	String id,
	@NotBlank(message = "비밀번호는 공백일 수 없습니다.")
	String password,

	@NotNull(message = "아이디 저장 여부는 필수입니다.")
	boolean isSaveId
) {
}
