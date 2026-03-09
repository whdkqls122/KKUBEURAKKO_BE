package com.kkubeurakko.kkubeurakko_be.domain.coupons.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kkubeurakko.kkubeurakko_be.domain.coupons.dto.AdminCouponResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.dto.CouponCreateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.dto.CouponResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.dto.IssuedCouponResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.service.CouponService;
import com.kkubeurakko.kkubeurakko_be.global.ApiResponse;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/coupons")
@RequiredArgsConstructor
@Tag(name = "Coupon API", description = "쿠폰 API")
public class CouponController {
	private final CouponService couponService;

	@Operation(
		summary = "발급 가능 쿠폰 목록 조회 API",
		description = "발급 가능한 쿠폰 목록을 조회 할 때 사용합니다."
	)
	@GetMapping("")
	public ResponseEntity<ApiResponse<List<CouponResponseDto>>> getAllCouponDoesNotHave(
		@AuthenticationPrincipal
		CustomOAuth2User customOAuth2User
	) {
		List<CouponResponseDto> couponResponseDtoList = couponService.getAllCouponDoesNotHave(customOAuth2User);

		return ResponseEntity.ok().body(
			ApiResponse.success(
				"발급 가능한 쿠폰 목록 조회가 완료되었습니다.",
				couponResponseDtoList
			)
		);
	}

	@Operation(
		summary = "발급된 쿠폰 목록 조회 API",
		description = "사용자의 발급된 쿠폰 목록을 조회 할 때 사용합니다."
	)
	@GetMapping("/issued")
	public ResponseEntity<ApiResponse<List<IssuedCouponResponseDto>>> getAllUserOwned(
		@AuthenticationPrincipal
		CustomOAuth2User customOAuth2User
	) {
		List<IssuedCouponResponseDto> couponResponseDtoList = couponService.getAllUserOwned(customOAuth2User);

		return ResponseEntity.ok().body(
			ApiResponse.success(
				"발급된 쿠폰 목록 조회가 완료되었습니다.",
				couponResponseDtoList
			)
		);
	}

	@Operation(
		summary = "쿠폰 발급 API",
		description = "쿠폰을 발급할 때 사용합니다."
	)
	@PostMapping("/issued/{couponId}")
	public ResponseEntity<ApiResponse<Void>> issueCoupon(
		@PathVariable("couponId")
		Long couponId,
		@AuthenticationPrincipal
		CustomOAuth2User customOAuth2User
	) {
		couponService.issueCoupon(couponId, customOAuth2User);

		return ResponseEntity.ok().body(
			ApiResponse.success(
				"쿠폰이 발급되었습니다.",
				null
			)
		);
	}

	@Operation(
		summary = "쿠폰 발행 API",
		description = "가게측에서 쿠폰을 발행합니다."
	)
	@PostMapping("/{storeId}")
	public ResponseEntity<ApiResponse<Void>> createCoupon(
		@PathVariable Long storeId,
		@RequestBody @Valid CouponCreateRequest request
	) {
		couponService.createCoupon(storeId, request);

		return ResponseEntity.ok().body(
			ApiResponse.success(
				"쿠폰이 성공적으로 발행되었습니다.",
				null
			)
		);
	}

	@Operation(
		summary = "쿠폰 조회 API",
		description = "가게측에서 쿠폰을 조회합니다."
	)
	@GetMapping("/{storeId}")
	public ResponseEntity<ApiResponse<List<AdminCouponResponseDto>>> getCoupons(
		@PathVariable Long storeId
	) {
		return ResponseEntity.ok(
			ApiResponse.success(
				"쿠폰 목록을 성공적으로 조회했습니다.",
				couponService.getCouponsByStore(storeId)
			)
		);
	}

	@Operation(
		summary = "쿠폰 숨김 처리 API",
		description = "가게측에서 쿠폰을 숨김 처리합니다."
	)
	@PatchMapping("/{storeId}/{couponId}/hide")
	public ResponseEntity<ApiResponse<Void>> hideCoupon(
		@PathVariable Long storeId,
		@PathVariable Long couponId
	) {
		couponService.hideCoupon(couponId, storeId);

		return ResponseEntity.ok(
			ApiResponse.success("쿠폰이 숨김 처리되었습니다.", null)
		);
	}

	@Operation(
		summary = "쿠폰 활성화 API",
		description = "숨김 상태의 쿠폰을 다시 활성화합니다."
	)
	@PatchMapping("/{storeId}/{couponId}/show")
	public ResponseEntity<ApiResponse<Void>> showCoupon(
		@PathVariable Long storeId,
		@PathVariable Long couponId
	) {
		couponService.showCoupon(couponId, storeId);

		return ResponseEntity.ok(
			ApiResponse.success("쿠폰이 다시 활성화되었습니다.", null)
		);
	}
}
