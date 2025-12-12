package com.kkubeurakko.kkubeurakko_be.domain.orders.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderCreateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderDetailResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.orders.service.OrderService;
import com.kkubeurakko.kkubeurakko_be.global.ApiResponse;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order API", description = "주문 관련 API")
public class OrderController {

	private final OrderService orderService;

	@Operation(
		summary = "주문 내역 목록 조회",
		description = "주문 내역 목록을 조회합니다."
	)
	@GetMapping("")
	public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getAllOrderHistory(
		@AuthenticationPrincipal
		CustomOAuth2User customOAuth2User
	) {
		List<OrderResponseDto> orderResponseDtoList = orderService.getAllOrderHistory(customOAuth2User);

		return ResponseEntity.ok().body(
			ApiResponse.success(
				"주문 내역 목록 조회가 완료되었습니다.",
				orderResponseDtoList
			)
		);
	}

	@Operation(
		summary = "주문 상세 내역 조회",
		description = "주문 상세 내역을 조회합니다."
	)
	@GetMapping("/detail/{orderId}")
	public ResponseEntity<ApiResponse<OrderDetailResponseDto>> getOrderDetailHistory(
		@PathVariable("orderId")
		Long orderId,
		@AuthenticationPrincipal
		CustomOAuth2User customOAuth2User
	) {
		OrderDetailResponseDto orderDetailResponseDto = orderService.getOrderDetailHistory(orderId, customOAuth2User);

		return ResponseEntity.ok().body(
			ApiResponse.success(
				"주문 상세 내역 조회가 완료되었습니다.",
				orderDetailResponseDto
			)
		);
	}

	@Operation(
		summary = "주문 요청",
		description = "결제를 완료하고 가게에 주문 요청을 보냅니다."
	)
	@PostMapping("")
	public ResponseEntity<ApiResponse<Long>> createOrder(
		@Valid @RequestBody OrderCreateRequest request,
		@AuthenticationPrincipal CustomOAuth2User customOAuth2User
	) {
		Long orderId = orderService.createOrder(request, customOAuth2User);
		return ResponseEntity.ok().body(
			ApiResponse.success(
				"주문 요청이 완료되었습니다.",
				orderId
			)
		);
	}
}
