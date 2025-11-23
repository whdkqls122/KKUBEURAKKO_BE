package com.kkubeurakko.kkubeurakko_be.domain.orders.service;

import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderResponseDto;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

public interface OrderService {
	//주문 내역 조회 메서드
	List<OrderResponseDto> getAllOrderHistory(CustomOAuth2User customOAuth2User);
}
