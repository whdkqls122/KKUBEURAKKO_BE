package com.kkubeurakko.kkubeurakko_be.domain.users.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kkubeurakko.kkubeurakko_be.domain.users.dto.UserInfoResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.dto.UserVerifyRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.service.UserService;
import com.kkubeurakko.kkubeurakko_be.global.ApiResponse;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/user")
@Tag(name = "User API", description = "사용자 관련 API")
public class UserController {
	private final UserService userService;

	@Operation(
		summary = "사용자 정보 조회 API",
		description = "사용자 정보를 조회합니다."
	)
	@GetMapping("/info")
	public ResponseEntity<ApiResponse<UserInfoResponseDto>> getUserInfo(
		@AuthenticationPrincipal
		CustomOAuth2User customOAuth2User
	) {
		UserInfoResponseDto userInfoResponseDto = userService.getUserInfo(customOAuth2User);

		return ResponseEntity.ok().body(
			ApiResponse.success(
				"사용자 정보 조회가 완료되었습니다.",
				userInfoResponseDto
			)
		);
	}

	@Operation(
		summary = "사용자 인증 정보 조회 후 휴대폰 번호 저장 API",
		description = "사용자 인증 정보 ID값을 받아 조회 후 휴대폰 번호를 저장합니다."
	)
	@PutMapping("/verify-phone")
	public ResponseEntity<ApiResponse<Void>> updateUserPhone(
		@RequestBody UserVerifyRequestDto userVerifyRequestDto,
		@AuthenticationPrincipal
		CustomOAuth2User customOAuth2User
	) {
		userService.saveUserPhone(userVerifyRequestDto, customOAuth2User);

		return ResponseEntity.ok().body(
			ApiResponse.success(
				"회원 가입이 완료되었습니다.",
				null
			)
		);
	}
}
