package com.kkubeurakko.kkubeurakko_be.domain.orders.service;

import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderCreateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderDetailResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderSocketResponse;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Status;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

public interface OrderService {
	//주문 내역 조회 메서드
	List<OrderResponseDto> getAllOrderHistory(CustomOAuth2User customOAuth2User);

	//주문 내역 상세 조회
	OrderDetailResponseDto getOrderDetailHistory(Long orderId, CustomOAuth2User customOAuth2User);

	//주문 생성/요청
	Long createOrder(OrderCreateRequest request, CustomOAuth2User customOAuth2User);

	void acceptOrder(Long orderId, int completionTime);

	void startDelivery(Long orderId);

	void completeOrder(Long orderId);

	void cancelOrder(Long orderId);

	void refundOrder(Long orderId);

	List<OrderSocketResponse> getOrdersByStoreAndStatus(Long storeId, Status status);

	boolean isInProgressOrder(Users user);
}
