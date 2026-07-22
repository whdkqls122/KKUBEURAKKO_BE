package com.kkubeurakko.kkubeurakko_be.domain.survey.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kkubeurakko.kkubeurakko_be.domain.survey.dto.SurveyRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.survey.service.SurveyServiceImpl;
import com.kkubeurakko.kkubeurakko_be.global.ApiResponse;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/surveys")
@Tag(name = "Survey API", description = "설문 조사 API")
public class SurveyController {

	private final SurveyServiceImpl surveyService;

	@Operation(
		summary = "설문 참여 여부 조회",
		description = "설문 참여 여부를 조회합니다."
	)
	@GetMapping("/exists")
	public ResponseEntity<ApiResponse<Boolean>> checkSurvey(
		@AuthenticationPrincipal CustomOAuth2User user
	) {
		boolean result = surveyService.isSurveySubmitted(user.getName());

		return ResponseEntity.ok(
			ApiResponse.success("설문 참여 여부", result)
		);
	}

	@Operation(
		summary = "설문 응답 저장",
		description = "설문 응답을 저장합니다."
	)
	@PostMapping("/complete")
	public ResponseEntity<ApiResponse<Boolean>> completeSurvey(
		@AuthenticationPrincipal CustomOAuth2User user,
		@Valid @RequestBody SurveyRequestDto surveyRequestDto
	) {
		surveyService.completeSurvey(user.getName(), surveyRequestDto);

		return ResponseEntity.ok(
			ApiResponse.success(
				"설문 참여가 완료되었습니다.\n"
					+ "쿠폰 지급까지 최대 5분정도 소요될 수 있습니다.", null
			)
		);
	}
}