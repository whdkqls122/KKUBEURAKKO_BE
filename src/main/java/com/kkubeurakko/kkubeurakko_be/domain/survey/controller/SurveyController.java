package com.kkubeurakko.kkubeurakko_be.domain.survey.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kkubeurakko.kkubeurakko_be.domain.survey.service.SurveyServiceImpl;
import com.kkubeurakko.kkubeurakko_be.global.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/surveys")
public class SurveyController {

	private final SurveyServiceImpl surveyService;

	@Operation(
		summary = "설문 참여 여부 조회",
		description = "설문 참여 여부를 조회합니다."
	)
	@GetMapping("/exists/{userNumber}")
	public ResponseEntity<ApiResponse<Boolean>> checkSurvey(
		@PathVariable String userNumber
	) {
		boolean result = surveyService.hasParticipated(userNumber);
		return ResponseEntity.ok(
			ApiResponse.success("설문 참여 여부", result)
		);
	}
}