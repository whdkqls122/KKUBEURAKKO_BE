package com.kkubeurakko.kkubeurakko_be.domain.orders.service;

import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderCreateRequest;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

public interface OrderCreateService {
	//주문 생성/요청
	Long createOrder(OrderCreateRequest request, CustomOAuth2User customOAuth2User);
}