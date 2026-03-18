package com.kkubeurakko.kkubeurakko_be.domain.orders.config;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderSocketResponse;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Orders;
import com.kkubeurakko.kkubeurakko_be.global.security.util.AesUtil;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderSocketSender {

	private final SimpMessagingTemplate messagingTemplate;
	private final AesUtil aesUtil;

	public void sendOrder(Long storeId, Orders order) {

		String address = null;
		if (order.getAddressSnapshot() != null) {
			String road = order.getAddressSnapshot().getRoadName();
			String detail = order.getAddressSnapshot().getDetailAddress();

			if (detail != null) {
				detail = aesUtil.decryptFromBase64(detail);
			}

			address = (road != null ? road : "") +
				(detail != null ? " " + detail : "");
		}

		String phone = aesUtil.decryptFromBase64(order.getUser().getPhone());

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
