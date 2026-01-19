package com.kkubeurakko.kkubeurakko_be.domain.orders.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.kkubeurakko.kkubeurakko_be.TestFixture;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.entity.Addresses;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.repository.AddressRepository;
import com.kkubeurakko.kkubeurakko_be.domain.cart.entity.Cart;
import com.kkubeurakko.kkubeurakko_be.domain.cart.repository.CartRepository;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.repository.UserCouponRepository;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.Menus;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.Options;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderCreateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderDetailResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderType;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Orders;
import com.kkubeurakko.kkubeurakko_be.domain.orders.repository.OrderRepository;
import com.kkubeurakko.kkubeurakko_be.domain.payments.repository.PaymentRepository;
import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.PortOnePaymentResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.portone.service.PortOneService;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.repository.UserRepository;
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
	@Mock
	private UserRepository userRepository;
	@Mock
	private CartRepository cartRepository;
	@Mock
	private AddressRepository addressRepository;
	@Mock
	private PaymentRepository paymentRepository;
	@Mock
	private UserCouponRepository userCouponRepository;
	@Mock
	private SimpMessagingTemplate messagingTemplate;

	@Mock
	private PortOneService portOneService;

	@Mock
	private ApplicationEventPublisher eventPublisher;

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
		when(aesUtil.decryptFromBase64("암호화된 상세주소")).thenReturn("101동 1004호");
		// when
		OrderDetailResponseDto result = orderService.getOrderDetailHistory(1L, customUser);

		// then
		assertEquals(order.getId(), result.id());
		//assertEquals(order.getAddress(), result.address());
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

	@Test
	@DisplayName("배달 주문 생성이 정상적으로 처리되는지 검증한다")
	void createOrder() {

		// given
		Users user = TestFixture.createUser("kakao_1234", "01012345678");
		Cart cart = TestFixture.createCartWithOneItem(user);
		Addresses address = TestFixture.createAddress(1L, user);

		CustomOAuth2User customOAuth2User = mock(CustomOAuth2User.class);
		when(customOAuth2User.getName()).thenReturn("kakao_1234");

		OrderCreateRequest request = new OrderCreateRequest(
			1L,
			"젓가락 2개 주세요",
			"초인종 누르지 말아주세요",
			OrderType.DELIVERY,
			null,
			"paymentid",
			20000,
			3000
		);

		when(userService.getUser("kakao_1234")).thenReturn(user);
		when(cartRepository.findByUserNumber(user.getUserNumber())).thenReturn(Optional.of(cart));
		when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

		Orders savedOrder = Orders.builder().id(10L).build();
		when(orderRepository.save(any())).thenReturn(savedOrder);

		PortOnePaymentResponseDto portOneResponse =
			TestFixture.createPortOnePaymentResponse(
				"paymentid",
				"tx-123",
				20000
			);

		when(portOneService.getPortOnePayment("paymentid"))
			.thenReturn(portOneResponse);

		// when
		Long orderId = orderService.createOrder(request, customOAuth2User);
		System.out.println(orderId);
		// then
		assertThat(orderId).isEqualTo(10L);
	}

}
