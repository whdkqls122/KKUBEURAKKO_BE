package com.kkubeurakko.kkubeurakko_be.domain.orders.config;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderSocketResponse;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderStatusSocketEvent;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Orders;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderSocketSender {

	private final SimpMessagingTemplate messagingTemplate;

	public void sendOrder(Long storeId, Orders order) {
		messagingTemplate.convertAndSend(
			"/topic/store/" + storeId,
			OrderSocketResponse.from(order)
		);
	}

	public void sendStatus(Long storeId, Orders order) {
		messagingTemplate.convertAndSend(
			"/topic/store/" + storeId + "/status",
			OrderStatusSocketEvent.from(order)
		);
	}
}
