package com.kkubeurakko.kkubeurakko_be.domain.users.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kkubeurakko.kkubeurakko_be.domain.auth.service.TokenService;
import com.kkubeurakko.kkubeurakko_be.domain.users.dto.UserInfoResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.dto.UserPhoneResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.dto.UserVerifyRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.service.UserService;
import com.kkubeurakko.kkubeurakko_be.domain.users.service.UserVerifyFacadeService;
import com.kkubeurakko.kkubeurakko_be.global.ApiResponse;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/user")
@Tag(name = "User API", description = "사용자 관련 API")
public class UserController {
	private final UserService userService;
	private final UserVerifyFacadeService userVerifyFacadeService;
	private final TokenService tokenService;

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
		summary = "사용자 휴대전화번호 조회 API",
		description = "사용자 휴대전화번호를 조회합니다."
	)
	@GetMapping("/info")
	public ResponseEntity<ApiResponse<UserPhoneResponseDto>> getUserPhone(
		@AuthenticationPrincipal
		CustomOAuth2User customOAuth2User
	) {
		UserPhoneResponseDto userPhoneResponseDto = userService.getUserPhone(customOAuth2User);

		return ResponseEntity.ok().body(
			ApiResponse.success(
				"사용자 정보 조회가 완료되었습니다.",
				userPhoneResponseDto
			)
		);
	}

	@Operation(
		summary = "사용자 인증 정보 조회 후 사용자 정보 API",
		description = "사용자 인증 정보 ID값을 받아 조회 후 사용자 정보를 저장합니다."
	)
	@PostMapping("/verify-phone")
	public ResponseEntity<ApiResponse<Void>> verifyAndSaveUser(
		@RequestBody UserVerifyRequestDto userVerifyRequestDto,
		HttpServletRequest request,
		HttpServletResponse response
	) {
		userVerifyFacadeService.verifyAndSaveUser(userVerifyRequestDto, request, response);

		return ResponseEntity.ok().body(
			ApiResponse.success(
				"회원 가입이 완료되었습니다.",
				null
			)
		);
	}

	@Operation(
		summary = "회원가입 완료 이후 토큰 발급 요청 API",
		description = "회원가입 완료 이후 Access/Refresh 토큰 발급을 요청하는 API 입니다."
	)
	@GetMapping("/complete-signup")
	public ResponseEntity<ApiResponse<Void>> completeSignup(
		HttpServletRequest request,
		HttpServletResponse response
	) {
		String accessToken = tokenService.createToken(request, response);

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", accessToken);

		return ResponseEntity.ok()
			.headers(headers)
			.body(
				ApiResponse.success(
					"토큰이 발급되었습니다.",
					null
				)
			);
	}

	@Operation(
		summary = "사용자 탈퇴 API",
		description = "사용자 탈퇴 요청 및 OAuth 연결 해제를 진행합니다."
	)
	@DeleteMapping("")
	public ResponseEntity<ApiResponse<Void>> deleteUser(
		@AuthenticationPrincipal
		CustomOAuth2User customOAuth2User,
		HttpServletRequest request,
		HttpServletResponse response
	) {
		userVerifyFacadeService.deleteUserAndUnlink(customOAuth2User, request, response);

		return ResponseEntity.ok().body(
			ApiResponse.success(
				"회원 탈퇴가 완료되었습니다.",
				null
			)
		);
	}
}
