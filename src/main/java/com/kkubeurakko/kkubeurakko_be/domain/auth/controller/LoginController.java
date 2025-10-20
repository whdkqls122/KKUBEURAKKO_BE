package com.kkubeurakko.kkubeurakko_be.domain.auth.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.LoginRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.auth.service.LoginService;
import com.kkubeurakko.kkubeurakko_be.global.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "Login API", description = "로그인 API")
@RequestMapping("/v1/admin/login")
public class LoginController {
	private final LoginService loginService;

	@Operation(
		summary = "관리자 로그인",
		description = "관리자가 로그인할 때 사용합니다."
	)
	@PostMapping("")
	public ResponseEntity<ApiResponse<Void>> adminLogin(
		@RequestBody @Valid LoginRequestDto loginRequestDto,
		HttpServletResponse response
	) {
		String accessToken = loginService.adminLogin(loginRequestDto, response);
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", accessToken);
		return ResponseEntity.ok()
			.headers(headers)
			.body(ApiResponse.success("로그인 성공", null));

	}
}
