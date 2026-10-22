package com.kkubeurakko.kkubeurakko_be.domain.orders.config;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.kkubeurakko.kkubeurakko_be.TestFixture;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.Menus;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderSocketResponse;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Orders;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Status;
import com.kkubeurakko.kkubeurakko_be.domain.orders.service.OrderDecryptService;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;

@ExtendWith(MockitoExtension.class)
class OrderSocketSenderTest {

	@Mock
	SimpMessagingTemplate messagingTemplate;
	@Mock
	OrderDecryptService orderDecryptService;
	@InjectMocks
	OrderSocketSender orderSocketSender;

	@Test
	@DisplayName("주문 상태 변경 WebSocket 송신을 검증한다.")
	void sendStatus() {
		OrderSocketResponse orderSocketResponse = OrderSocketResponse.builder()
			.orderId(1L)
			.status("ACCEPTED")
			.build();

		orderSocketSender.sendStatus(1L, orderSocketResponse);

		verify(messagingTemplate).convertAndSend("/topic/store/1", orderSocketResponse);
	}

}
