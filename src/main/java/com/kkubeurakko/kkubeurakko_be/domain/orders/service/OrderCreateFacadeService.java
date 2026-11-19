package com.kkubeurakko.kkubeurakko_be.domain.orders.service;

import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderCreateRequest;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

public interface OrderCreateFacadeService {
	//주문 생성/요청 (결제 확인, 현금영수증 발급, 주문 저장, 실패 시 결제 취소 요청 조율)
	Long createOrder(OrderCreateRequest request, CustomOAuth2User customOAuth2User);
}