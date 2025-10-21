package com.kkubeurakko.kkubeurakko_be.domain.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.SmsRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.SmsVerifyDto;
import com.kkubeurakko.kkubeurakko_be.domain.auth.service.SmsService;
import com.kkubeurakko.kkubeurakko_be.global.ApiResponse;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "SMS API", description = "SMS 관련 API")
@RequestMapping("/v1/sms")
public class SmsController {
	private final SmsService smsService;

	@Operation(
		summary = "SMS 인증번호 발송",
		description = "SMS 인증을 위한 인증번호 발송 API입니다."
	)
	@PostMapping("/send")
	public ResponseEntity<ApiResponse<String>> SendSms(
		@RequestBody @Valid SmsRequestDto smsRequestDto
	) {
		smsService.sendSms(smsRequestDto);
		return ResponseEntity.status(HttpStatus.OK).body(
			ApiResponse.success(
				"인증번호가 발송되었습니다.",
				null
			)
		);
	}

	@Operation(
		summary = "SMS 인증번호 검증",
		description = "SMS 인증번호 검증을 위한 API입니다."
	)
	@PostMapping("/verify")
	public ResponseEntity<ApiResponse<String>> verifyCodeForMember(
		@RequestBody @Valid SmsVerifyDto smsVerifyDto,
		@AuthenticationPrincipal CustomOAuth2User customOAuth2User
	) {
		smsService.verifyCodeForCustomer(smsVerifyDto, customOAuth2User);
		return ResponseEntity.status(HttpStatus.OK).body(
			ApiResponse.success(
				"인증되었습니다.",
				null
			)
		);
	}
}
