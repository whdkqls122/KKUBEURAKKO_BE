package com.kkubeurakko.kkubeurakko_be.domain.orders.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kkubeurakko.kkubeurakko_be.TestFixture;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.Menus;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.Options;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderDetailResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Orders;
import com.kkubeurakko.kkubeurakko_be.domain.orders.repository.OrderRepository;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.service.UserService;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;
import com.kkubeurakko.kkubeurakko_be.global.security.util.AesUtil;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private UserService userService;

	@Mock
	private AesUtil aesUtil;

	@InjectMocks
	private OrderServiceImpl orderService;

	@Test
	@DisplayName("주문 내역 목록이 정상적으로 조회 되는지 검증한다.")
	void getAllOrderHistory() {
		// given
		CustomOAuth2User customUser = mock(CustomOAuth2User.class);
		Users user = TestFixture.createUser("user1", "암호화된 전화번호");

		Menus menu = TestFixture.createMenu(1L, TestFixture.createMenuGroup(1L, TestFixture.createStore()));
		Orders order = TestFixture.createOrderWithPayment(1L, user, List.of(menu), List.of(List.of()));

		when(customUser.getName()).thenReturn("user1");
		when(userService.getUser("user1")).thenReturn(user);
		when(orderRepository.findByUser(user)).thenReturn(List.of(order));

		// when
		List<OrderResponseDto> result = orderService.getAllOrderHistory(customUser);

		// then
		assertEquals(1, result.size());
		assertEquals(menu.getName(), result.get(0).name());
		assertEquals(2, result.get(0).orderQuantity());
		assertEquals(order.getStatus(), result.get(0).status());
	}

	@Test
	@DisplayName("주문내역 상세내용이 정상적으로 조회되는지 검증한다.")
	void getOrderDetailHistory() {
		// given
		CustomOAuth2User customUser = mock(CustomOAuth2User.class);
		Users user = TestFixture.createUser("user1", "암호화된 전화번호");

		Menus menu = TestFixture.createMenu(1L, TestFixture.createMenuGroup(1L, TestFixture.createStore()));
		Options option = TestFixture.createOption(1L, "맵기 선택", 0, TestFixture.createOptionGroup(1L));
		Orders order = TestFixture.createOrderWithPayment(1L, user, List.of(menu), List.of(List.of(option)));

		when(customUser.getName()).thenReturn("user1");
		when(userService.getUser("user1")).thenReturn(user);
		when(orderRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(order));
		when(aesUtil.decryptFromBase64("암호화된 전화번호")).thenReturn("010-8337-6023");

		// when
		OrderDetailResponseDto result = orderService.getOrderDetailHistory(1L, customUser);

		// then
		assertEquals(order.getId(), result.id());
		assertEquals(order.getAddress(), result.address());
		assertEquals("010-8337-6023", result.phoneNum());
		assertEquals(1, result.orderMenuList().size());
		assertEquals(menu.getName(), result.orderMenuList().get(0).name());
		assertEquals(1, result.orderMenuList().get(0).optionList().size());
		assertEquals(option.getName(), result.orderMenuList().get(0).optionList().get(0).name());
	}

	@Test
	@DisplayName("주문 상세 내역이 조회 되지 않았을 때 정상적으로 예외가 발생하는지 검증한다.")
	void getOrderDetailHistory_orderNotFound() {
		// given
		CustomOAuth2User customUser = mock(CustomOAuth2User.class);
		Users user = TestFixture.createUser("user1", "암호화된 전화번호");

		when(customUser.getName()).thenReturn("user1");
		when(userService.getUser("user1")).thenReturn(user);
		when(orderRepository.findByIdAndUser(1L, user)).thenReturn(Optional.empty());

		// when & then
		assertThrows(IllegalArgumentException.class,
			() -> orderService.getOrderDetailHistory(1L, customUser),
			"조회된 주문 내역이 없습니다."
		);
	}
}
