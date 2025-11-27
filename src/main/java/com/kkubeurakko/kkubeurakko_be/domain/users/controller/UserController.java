package com.kkubeurakko.kkubeurakko_be.domain.users.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kkubeurakko.kkubeurakko_be.domain.users.dto.UserInfoResponseDto;
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
}
