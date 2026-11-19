package com.kkubeurakko.kkubeurakko_be.domain.orders.service;

import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderCreateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.PortOnePaymentResponseDto;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

public interface OrderCreateService {
	//결제 확인이 끝난 주문을 검증하고 주문 및 결제를 저장 (하나의 트랜잭션)
	Long createOrder(
		OrderCreateRequest request,
		CustomOAuth2User customOAuth2User,
		PortOnePaymentResponseDto paymentResponse
	);
}