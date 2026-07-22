package com.kkubeurakko.kkubeurakko_be.domain.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.ExistUserRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.NextStepDto;
import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.ResetPasswordMeRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.ResetPasswordRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.UserIdResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.auth.service.AccountService;
import com.kkubeurakko.kkubeurakko_be.domain.users.dto.UserVerifyRequestDto;
import com.kkubeurakko.kkubeurakko_be.global.ApiResponse;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "Account API", description = "계정 관련 API")
@RequestMapping("/v1/account")
public class AccountController {
	private final AccountService accountService;

	@Operation(
		summary = "아이디 찾기 API",
		description = "본인인증 완료 후 마스킹된 id를 찾아 반환하는 API입니다."
	)
	@PostMapping("/id")
	public ResponseEntity<ApiResponse<UserIdResponseDto>> findUserId(
		@RequestBody UserVerifyRequestDto userVerifyRequestDto
	) {

		UserIdResponseDto userIdResponseDto = accountService.findUserId(userVerifyRequestDto);

		return ResponseEntity.ok().body(
			ApiResponse.success(
				"아이디 찾기가 완료되었습니다.",
				userIdResponseDto
			)
		);
	}

	@Operation(
		summary = "아이디 존재 확인 API",
		description = "아이디가 존재하는 아이디인지 확인하는 API입니다."
	)
	@PostMapping("/id/check")
	public ResponseEntity<ApiResponse<Void>> existUserId(
		HttpServletResponse response,
		@RequestBody ExistUserRequestDto existUserRequestDto
	) {
		accountService.existUser(response, existUserRequestDto);

		return ResponseEntity.ok(
			ApiResponse.success("아이디 확인 완료", null)
		);
	}

	@Operation(
		summary = "본인인증 진행 후 다음 단계 조회 API",
		description = "모바일에서 본인인증 시 프론트에서의 상태값이 날아가는 이유로 현재 진행 탭과 다음 단계 진행 조회를 위한 API입니다."
	)
	@GetMapping("/next-step")
	public ResponseEntity<ApiResponse<NextStepDto>> getNextStep(HttpServletRequest request) {
		NextStepDto nextStepDto = accountService.getNextStep(request);

		return ResponseEntity.ok(
			ApiResponse.success("다음 단계 조회 완료", nextStepDto)
		);
	}

	@Operation(
		summary = "비밀번호 변경 전 본인인증 확인 API",
		description = "비밀번호 변경 전 본인인증 확인 후 사용자가 가입된 사용자가 맞는지 확인하는 API 입니다."
	)
	@PostMapping("/password/verification")
	public ResponseEntity<ApiResponse<Void>> verifyForResetPassword(
		HttpServletRequest request,
		HttpServletResponse response,
		@RequestBody UserVerifyRequestDto userVerifyRequestDto
	) {

		accountService.verifyForResetPassword(request, response, userVerifyRequestDto);

		return ResponseEntity.ok(
			ApiResponse.success("본인 인증 완료", null)
		);
	}

	@Operation(
		summary = "비로그인 비밀번호 재설정 API",
		description = "비로그인 상태에서 비밀번호를 재설정하는 API 입니다."
	)
	@PatchMapping("/password")
	public ResponseEntity<ApiResponse<Void>> resetPassword(
		HttpServletRequest request,
		HttpServletResponse response,
		@RequestBody ResetPasswordRequestDto resetPasswordRequestDto
	) {
		accountService.resetPassword(request, response, resetPasswordRequestDto);

		return ResponseEntity.ok(
			ApiResponse.success(
				"비밀번호 변경이 완료되었습니다.",
				null
			)
		);
	}

	@Operation(
		summary = "비밀번호 재설정 API",
		description = "비밀번호를 재설정하는 API 입니다."
	)
	@PatchMapping("/password/me")
	public ResponseEntity<ApiResponse<Void>> resetPasswordMe(
		@AuthenticationPrincipal CustomOAuth2User customOAuth2User,
		@RequestBody ResetPasswordMeRequestDto resetPasswordRequestDto
	) {
		accountService.resetPasswordMe(customOAuth2User, resetPasswordRequestDto);

		return ResponseEntity.ok(
			ApiResponse.success(
				"비밀번호 변경이 완료되었습니다.",
				null
			)
		);
	}
}
