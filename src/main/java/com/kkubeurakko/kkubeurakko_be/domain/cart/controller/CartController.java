package com.kkubeurakko.kkubeurakko_be.domain.cart.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kkubeurakko.kkubeurakko_be.domain.cart.dto.CartRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.cart.dto.CartResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.cart.service.CartService;
import com.kkubeurakko.kkubeurakko_be.global.ApiResponse;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/cart")
@Tag(name = "Cart API", description = "장바구니 API")
public class CartController {
	private final CartService cartService;

	@Operation(
		summary = "장바구니 조회",
		description = "장바구니 목록을 조회할 때 사용합니다."
	)
	@GetMapping("")
	public ResponseEntity<ApiResponse<CartResponseDto>> getCartList(
		@AuthenticationPrincipal
		CustomOAuth2User customOAuth2User
	) {
		CartResponseDto cartResponseDto = cartService.getCartList(customOAuth2User);

		return ResponseEntity.ok().body(
			ApiResponse.success(
				"장바구니 목록이 조회되었습니다.",
				cartResponseDto
			)
		);
	}

	@Operation(
		summary = "장바구니 등록",
		description = "장바구니에 메뉴를 등록할 때 사용합니다."
	)
	@PostMapping("")
	public ResponseEntity<ApiResponse<Void>> createCartItem(
		@Valid @RequestBody
		CartRequestDto cartRequestDto,
		@AuthenticationPrincipal
		CustomOAuth2User customOAuth2User
	) {
		cartService.createCartItem(cartRequestDto, customOAuth2User);
		return ResponseEntity.ok().body(
			ApiResponse.success(
				"장바구니 등록이 완료되었습니다.",
				null
			)
		);
	}

	@Operation(
		summary = "장바구니 수정",
		description = "장바구니의 메뉴 옵션 및 수량을 수정할 때 사용합니다."
	)
	@PutMapping("/{cartItemId}")
	public ResponseEntity<ApiResponse<Void>> updateCartItem(
		@PathVariable("cartItemId")
		Long cartItemId,
		@Valid @RequestBody
		CartRequestDto cartRequestDto,
		@AuthenticationPrincipal
		CustomOAuth2User customOAuth2User
	) {
		cartService.updateCartItem(cartItemId, cartRequestDto, customOAuth2User);
		return ResponseEntity.ok().body(
			ApiResponse.success(
				"장바구니 수정이 완료되었습니다.",
				null
			)
		);
	}

	@Operation(
		summary = "장바구니 삭제",
		description = "장바구니에 등록된 메뉴를 삭제할 때 사용합니다."
	)
	@DeleteMapping("/{cartItemId}")
	public ResponseEntity<ApiResponse<Void>> createCartItem(
		@PathVariable("cartItemId")
		Long cartItemId,
		@AuthenticationPrincipal
		CustomOAuth2User customOAuth2User
	) {
		cartService.deleteCartItem(cartItemId, customOAuth2User);
		return ResponseEntity.ok().body(
			ApiResponse.success(
				"장바구니 삭제가 완료되었습니다.",
				null
			)
		);
	}
}
