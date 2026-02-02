package com.kkubeurakko.kkubeurakko_be.domain.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.CheckDuplicateDto;
import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.SignupRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.auth.service.SignupService;
import com.kkubeurakko.kkubeurakko_be.global.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/signup")
@Tag(name = "Signup API", description = "AccessToken 재발급 API")
public class SignupController {
	private final SignupService signupService;

	@Operation(
		summary = "일반 회원가입 임시 사용자 생성 API",
		description = "소셜 로그인이 아닌 일반 로그인 사용자의 회원가입 임시 사용자 생성 API 입니다."
	)
	@PostMapping("/init")
	public ResponseEntity<ApiResponse<Void>> signup(
		HttpServletResponse response,
		@RequestBody @Valid SignupRequestDto signupRequestDto
	) {
		signupService.signupInit(response, signupRequestDto);
		return ResponseEntity.ok()
			.body(
				ApiResponse.success(
					"임시 사용자 생성이 완료되었습니다."
					, null
				)
			);
	}

	//아이디 중복 확인
	@Operation(
		summary = "아이디 중복 확인 API",
		description = "일반 로그인 사용자의 회원가입 중복 아이디 API 입니다."
	)
	@PostMapping("/duplicate")
	public ResponseEntity<ApiResponse<Void>> checkDuplicatedId(@RequestBody CheckDuplicateDto checkDuplicateDto) {
		return ResponseEntity.ok().body(
			signupService.checkDuplicatedId(checkDuplicateDto)
		);
	}
}
