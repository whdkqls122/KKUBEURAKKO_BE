package com.kkubeurakko.kkubeurakko_be.domain.auth.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kkubeurakko.kkubeurakko_be.domain.auth.service.ReissueService;
import com.kkubeurakko.kkubeurakko_be.global.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
@Tag(name = "Reissue API", description = "AccessToken 재발급 API")
public class ReissueController {
	private final ReissueService reissueService;

	@Operation(
		summary = "AccessToken 재발급",
		description = "저장되어있는 RefreshToken을 통해 AccessToken을 재발급 합니다."
	)
	@GetMapping("/reissue")
	public ResponseEntity<ApiResponse<Void>> reissue(HttpServletRequest request) {
		String newAccessToken = reissueService.reissue(request);
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", newAccessToken);
		return ResponseEntity.ok()
			.headers(headers)
			.body(ApiResponse.success("토큰이 재발급 되었습니다.", null));
	}
}
