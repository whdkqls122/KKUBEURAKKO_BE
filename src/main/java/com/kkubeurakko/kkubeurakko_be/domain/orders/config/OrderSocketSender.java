package com.kkubeurakko.kkubeurakko_be.domain.orders.config;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderSocketResponse;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Orders;
import com.kkubeurakko.kkubeurakko_be.domain.orders.service.OrderDecryptService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderSocketSender {

	private final SimpMessagingTemplate messagingTemplate;
	private final OrderDecryptService orderDecryptService;

	public void sendOrder(Long storeId, Orders order) {

		String address = null;

		if (order.getAddressSnapshot() != null) {
			String road = order.getAddressSnapshot().getRoadName();
			String detail = order.getAddressSnapshot().getDetailAddress();

			address = (road != null ? road : "") +
				(detail != null ? " " + detail : "");
		}

		String phone = orderDecryptService
			.decryptPhone(order.getUser().getPhone());

		messagingTemplate.convertAndSend(
			"/topic/store/" + storeId,
			OrderSocketResponse.from(order, address, phone)
		);
	}

	public void sendStatus(Long storeId, OrderSocketResponse response) {
		messagingTemplate.convertAndSend(
			"/topic/store/" + storeId,
			response
		);
	}
}
