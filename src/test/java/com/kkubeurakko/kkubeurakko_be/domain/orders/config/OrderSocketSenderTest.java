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
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderStatusSocketEvent;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Orders;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Status;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;

@ExtendWith(MockitoExtension.class)
class OrderSocketSenderTest {

	@Mock
	SimpMessagingTemplate messagingTemplate;

	@InjectMocks
	OrderSocketSender orderSocketSender;

	@Test
	@DisplayName("주문 상태 변경 WebSocket 송신을 검증한다.")
	void orderStatusSocketEvent() {
		// given
		Stores store = TestFixture.createStore();
		Users user = TestFixture.createUser("user1", "010-1234-5678");

		Menus menu = TestFixture.createMenu(
			1L,
			TestFixture.createMenuGroup(1L, store)
		);

		Orders order = TestFixture.createOrderWithPayment(
			1L,
			user,
			List.of(menu),
			List.of(List.of())
		);

		ReflectionTestUtils.setField(order, "status", Status.PENDING);
		order.accept(30);

		// when
		orderSocketSender.sendStatus(store.getId(), order);

		// then
		ArgumentCaptor<OrderStatusSocketEvent> captor =
			ArgumentCaptor.forClass(OrderStatusSocketEvent.class);

		verify(messagingTemplate).convertAndSend(
			eq("/topic/store/" + store.getId() + "/status"),
			captor.capture()
		);

		OrderStatusSocketEvent event = captor.getValue();
		assertThat(event.getOrderId()).isEqualTo(1L);
		assertThat(event.getStatus()).isEqualTo("ACCEPTED");
		assertThat(event.getCompletionTime()).isEqualTo(30);
	}

}
