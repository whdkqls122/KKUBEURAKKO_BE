package com.kkubeurakko.kkubeurakko_be.domain.orders.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.test.util.ReflectionTestUtils;

import com.kkubeurakko.kkubeurakko_be.TestFixture;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.entity.Addresses;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.repository.AddressRepository;
import com.kkubeurakko.kkubeurakko_be.domain.cart.entity.Cart;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.repository.UserCouponRepository;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderCreateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderCreatedEvent;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderType;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Orders;
import com.kkubeurakko.kkubeurakko_be.domain.orders.exception.OrderCreateException;
import com.kkubeurakko.kkubeurakko_be.domain.orders.repository.OrderRepository;
import com.kkubeurakko.kkubeurakko_be.domain.orders.util.OrderNumberUtil;
import com.kkubeurakko.kkubeurakko_be.domain.payments.entity.Payments;
import com.kkubeurakko.kkubeurakko_be.domain.payments.exception.InvalidPaymentException;
import com.kkubeurakko.kkubeurakko_be.domain.payments.repository.PaymentRepository;
import com.kkubeurakko.kkubeurakko_be.domain.portone.exception.PortOnePaymentHistoryException;
import com.kkubeurakko.kkubeurakko_be.domain.portone.service.PortOneService;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.DeliveryRegionAbleResponse;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;
import com.kkubeurakko.kkubeurakko_be.domain.stores.service.DeliveryRegionService;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.service.UserService;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

@ExtendWith(MockitoExtension.class)
class OrderCreateServiceImplTest {

	@Mock
	private UserCouponRepository userCouponRepository;
	@Mock
	private PaymentRepository paymentRepository;
	@Mock
	private AddressRepository addressRepository;
	@Mock
	private OrderRepository orderRepository;
	@Mock
	private UserService userService;
	@Mock
	private PortOneService portOneService;
	@Mock
	private ApplicationEventPublisher eventPublisher;
	@Mock
	private OrderNumberUtil orderNumberUtil;
	@Mock
	private DeliveryRegionService deliveryRegionService;
	@Mock
	private RedisTemplate<String, String> redisTemplate;
	@Mock
	private ValueOperations<String, String> valueOperations;

	@InjectMocks
	private OrderCreateServiceImpl orderCreateService;

	private void givenPaymentLock(boolean acquired) {
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(acquired);
	}

	/** 락을 잡을 때 쓴 토큰으로만 해제를 요청했는지 확인한다. */
	private void verifyPaymentLockReleasedWithOwnToken() {
		ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
		verify(valueOperations).setIfAbsent(eq("order:payment-lock:paymentid"), tokenCaptor.capture(), any(Duration.class));
		verify(redisTemplate).execute(
			any(RedisScript.class),
			eq(List.of("order:payment-lock:paymentid")),
			eq(tokenCaptor.getValue())
		);
	}

	@Test
	@DisplayName("배달 주문 생성이 정상적으로 처리되는지 검증한다")
	void createOrder() {
		// given
		givenPaymentLock(true);
		Users user = TestFixture.createUser("kakao_1234", "01012345678");
		Cart cart = TestFixture.createCartWithOneItem(user);
		Addresses address = TestFixture.createAddress(1L, user);
		Stores stores = TestFixture.createStore();
		ReflectionTestUtils.setField(user, "cart", cart);
		ArgumentCaptor<Payments> paymentCaptor = ArgumentCaptor.forClass(Payments.class);
		CustomOAuth2User customOAuth2User = mock(CustomOAuth2User.class);
		when(customOAuth2User.getName()).thenReturn("kakao_1234");

		OrderCreateRequest request = new OrderCreateRequest(
			1L,
			"젓가락 2개 주세요",
			"초인종 누르지 말아주세요",
			OrderType.DELIVERY,
			null,
			"paymentid",
			false,
			null,
			36000,
			0
		);

		when(portOneService.getPaidPayment("paymentid"))
			.thenReturn(TestFixture.createPortOnePaymentResponse("paymentid", "tx-123", 36000));
		when(userService.getUser("kakao_1234")).thenReturn(user);
		when(addressRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(address));
		when(deliveryRegionService.isDeliverable(stores.getId(), address.getLatitude(), address.getLongitude()))
			.thenReturn(DeliveryRegionAbleResponse.builder().able(true).build());
		when(orderRepository.save(any())).thenReturn(Orders.builder().id(10L).build());

		// when
		Long orderId = orderCreateService.createOrder(request, customOAuth2User);

		// then
		assertThat(orderId).isEqualTo(10L);
		verify(paymentRepository).save(paymentCaptor.capture());
		assertThat(paymentCaptor.getValue().getPaymentId()).isEqualTo("paymentid");
		verify(redisTemplate, never()).execute(any(RedisScript.class), anyList(), anyString());

		// 주문 후처리
		assertThat(user.getCart()).isNull();
		assertThat(address.getLastUsedAt()).isNotNull();
		ArgumentCaptor<OrderCreatedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
		verify(eventPublisher).publishEvent(eventCaptor.capture());
		assertThat(eventCaptor.getValue().orderId()).isEqualTo(10L);
		assertThat(eventCaptor.getValue().storeId()).isEqualTo(stores.getId());
	}

	@DisplayName("배달 주문 생성 시 주문번호가 정상적으로 생성된다")
	@Test
	void createOrder_withOrderNumber() {
		// given
		givenPaymentLock(true);
		Users user = TestFixture.createUser("kakao_1234", "01012345678");
		Cart cart = TestFixture.createCartWithOneItem(user);
		Stores stores = TestFixture.createStore();
		Addresses address = TestFixture.createAddress(1L, user);
		ReflectionTestUtils.setField(user, "cart", cart);

		CustomOAuth2User customOAuth2User = mock(CustomOAuth2User.class);
		when(customOAuth2User.getName()).thenReturn("kakao_1234");

		OrderCreateRequest request = new OrderCreateRequest(
			1L, "요청사항", "배달메모", OrderType.DELIVERY, null,
			"paymentid", false, null, 36000, 0
		);

		when(portOneService.getPaidPayment("paymentid"))
			.thenReturn(TestFixture.createPortOnePaymentResponse("paymentid", "tx-123", 36000));
		when(userService.getUser("kakao_1234")).thenReturn(user);
		when(addressRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(address));
		when(deliveryRegionService.isDeliverable(stores.getId(), address.getLatitude(), address.getLongitude()))
			.thenReturn(DeliveryRegionAbleResponse.builder().able(true).build());
		when(orderNumberUtil.createOrderNumber(anyLong(), eq(OrderType.DELIVERY))).thenReturn("A-1");

		ArgumentCaptor<Orders> captor = ArgumentCaptor.forClass(Orders.class);
		when(orderRepository.save(captor.capture())).thenReturn(Orders.builder().id(10L).build());

		// when
		Long orderId = orderCreateService.createOrder(request, customOAuth2User);

		// then
		assertThat(orderId).isEqualTo(10L);
		assertThat(captor.getValue().getOrderNumber()).isEqualTo("A-1");
	}

	@DisplayName("사용할 수 없는 결제면 장바구니 확인과 결제 취소 이벤트 없이 거절된다")
	@Test
	void createOrder_invalidPayment_noRollbackEvent() {
		// given
		givenPaymentLock(true);
		CustomOAuth2User customOAuth2User = mock(CustomOAuth2User.class);
		OrderCreateRequest request = new OrderCreateRequest(
			1L, "요청사항", "배달메모", OrderType.DELIVERY, null,
			"paymentid", false, null, 36000, 0
		);
		when(portOneService.getPaidPayment("paymentid"))
			.thenThrow(new InvalidPaymentException("결제가 완료되지 않았습니다."));

		// when & then
		assertThrows(InvalidPaymentException.class, () -> orderCreateService.createOrder(request, customOAuth2User));

		verify(userService, never()).getUser(anyString());
		verify(eventPublisher, never()).publishEvent(any(Object.class));
		verify(orderRepository, never()).save(any());
		verifyPaymentLockReleasedWithOwnToken();
	}

	@DisplayName("이미 사용된 paymentId로 주문하면 결제 조회와 취소 이벤트 없이 거절된다")
	@Test
	void createOrder_duplicatePayment_noRollbackEvent() {
		// given
		CustomOAuth2User customOAuth2User = mock(CustomOAuth2User.class);
		OrderCreateRequest request = new OrderCreateRequest(
			1L, "요청사항", "배달메모", OrderType.DELIVERY, null,
			"paymentid", false, null, 36000, 0
		);
		when(paymentRepository.existsByPaymentId("paymentid")).thenReturn(true);

		// when & then
		assertThatThrownBy(() -> orderCreateService.createOrder(request, customOAuth2User))
			.isInstanceOf(InvalidPaymentException.class)
			.hasMessage("이미 사용된 결제입니다.");

		verify(portOneService, never()).getPaidPayment(anyString());
		verify(redisTemplate, never()).opsForValue();
		verify(eventPublisher, never()).publishEvent(any(Object.class));
		verify(orderRepository, never()).save(any());
	}

	@DisplayName("결제 금액이 다르면 결제 ID를 담은 취소 이벤트를 발행한다")
	@Test
	void createOrder_amountMismatch_publishesRollbackEvent() {
		// given
		givenPaymentLock(true);
		Users user = TestFixture.createUser("kakao_1234", "01012345678");
		Cart cart = TestFixture.createCartWithOneItem(user);
		Addresses address = TestFixture.createAddress(1L, user);
		Stores stores = TestFixture.createStore();
		ReflectionTestUtils.setField(user, "cart", cart);

		CustomOAuth2User customOAuth2User = mock(CustomOAuth2User.class);
		when(customOAuth2User.getName()).thenReturn("kakao_1234");

		OrderCreateRequest request = new OrderCreateRequest(
			1L, "요청사항", "배달메모", OrderType.DELIVERY, null,
			"paymentid", false, null, 36000, 0
		);

		// 장바구니 금액은 36,000원인데 결제는 30,000원
		when(portOneService.getPaidPayment("paymentid"))
			.thenReturn(TestFixture.createPortOnePaymentResponse("paymentid", "tx-123", 30000));
		when(userService.getUser("kakao_1234")).thenReturn(user);
		when(addressRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(address));
		when(deliveryRegionService.isDeliverable(stores.getId(), address.getLatitude(), address.getLongitude()))
			.thenReturn(DeliveryRegionAbleResponse.builder().able(true).build());

		// when & then
		assertThrows(OrderCreateException.class, () -> orderCreateService.createOrder(request, customOAuth2User));

		ArgumentCaptor<OrderCreateException> eventCaptor = ArgumentCaptor.forClass(OrderCreateException.class);
		verify(eventPublisher).publishEvent(eventCaptor.capture());
		assertThat(eventCaptor.getValue().getPaymentId()).isEqualTo("paymentid");
		verify(orderRepository, never()).save(any());
		verify(redisTemplate, never()).execute(any(RedisScript.class), anyList(), anyString());
	}

	@DisplayName("결제 확인 후 장바구니 검증에서 실패하면 결제 취소 이벤트를 발행한다")
	@Test
	void createOrder_emptyCartAfterPaid_publishesRollbackEvent() {
		// given: 장바구니 없는 사용자
		givenPaymentLock(true);
		Users user = TestFixture.createUser("kakao_1234", "01012345678");

		CustomOAuth2User customOAuth2User = mock(CustomOAuth2User.class);
		when(customOAuth2User.getName()).thenReturn("kakao_1234");

		OrderCreateRequest request = new OrderCreateRequest(
			1L, "요청사항", "배달메모", OrderType.DELIVERY, null,
			"paymentid", false, null, 36000, 0
		);

		when(portOneService.getPaidPayment("paymentid"))
			.thenReturn(TestFixture.createPortOnePaymentResponse("paymentid", "tx-123", 36000));
		when(userService.getUser("kakao_1234")).thenReturn(user);

		// when & then
		assertThrows(OrderCreateException.class, () -> orderCreateService.createOrder(request, customOAuth2User));

		ArgumentCaptor<OrderCreateException> eventCaptor = ArgumentCaptor.forClass(OrderCreateException.class);
		verify(eventPublisher).publishEvent(eventCaptor.capture());
		assertThat(eventCaptor.getValue().getPaymentId()).isEqualTo("paymentid");
	}

	@DisplayName("같은 결제가 이미 처리 중이면 결제 조회와 취소 이벤트 없이 거절된다")
	@Test
	void createOrder_paymentLocked_noRollbackEvent() {
		// given
		CustomOAuth2User customOAuth2User = mock(CustomOAuth2User.class);
		OrderCreateRequest request = new OrderCreateRequest(
			1L, "요청사항", "배달메모", OrderType.DELIVERY, null,
			"paymentid", false, null, 36000, 0
		);
		givenPaymentLock(false);

		// when & then
		assertThatThrownBy(() -> orderCreateService.createOrder(request, customOAuth2User))
			.isInstanceOf(InvalidPaymentException.class)
			.hasMessage("이미 처리 중이거나 사용된 결제입니다.");

		verify(portOneService, never()).getPaidPayment(anyString());
		verify(eventPublisher, never()).publishEvent(any(Object.class));
	}

	@DisplayName("PortOne 결제 조회에 실패하면 취소 이벤트 없이 거절하고 재시도할 수 있게 락을 해제한다")
	@Test
	void createOrder_paymentLookupFailed_noRollbackEventAndReleaseLock() {
		// given
		CustomOAuth2User customOAuth2User = mock(CustomOAuth2User.class);
		OrderCreateRequest request = new OrderCreateRequest(
			1L, "요청사항", "배달메모", OrderType.DELIVERY, null,
			"paymentid", false, null, 36000, 0
		);
		givenPaymentLock(true);
		when(portOneService.getPaidPayment("paymentid"))
			.thenThrow(new PortOnePaymentHistoryException("PortOne 결제 조회 실패: ", new RuntimeException("timeout")));

		// when & then
		assertThrows(PortOnePaymentHistoryException.class, () -> orderCreateService.createOrder(request, customOAuth2User));

		verifyPaymentLockReleasedWithOwnToken();
		verify(userService, never()).getUser(anyString());
		verify(eventPublisher, never()).publishEvent(any(Object.class));
	}

	@DisplayName("결제 락 획득 중 Redis 오류가 나면 결제 조회와 취소 이벤트 없이 실패한다")
	@Test
	void createOrder_lockError_noRollbackEvent() {
		// given
		CustomOAuth2User customOAuth2User = mock(CustomOAuth2User.class);
		OrderCreateRequest request = new OrderCreateRequest(
			1L, "요청사항", "배달메모", OrderType.DELIVERY, null,
			"paymentid", false, null, 36000, 0
		);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
			.thenThrow(new RedisConnectionFailureException("Redis 연결 실패"));

		// when & then
		assertThrows(RedisConnectionFailureException.class, () -> orderCreateService.createOrder(request, customOAuth2User));

		verify(portOneService, never()).getPaidPayment(anyString());
		verify(eventPublisher, never()).publishEvent(any(Object.class));
	}

	@DisplayName("같은 결제로 두 요청이 동시에 들어오면 먼저 들어온 요청만 주문을 생성하고 나머지는 취소 이벤트 없이 거절된다")
	@Test
	void createOrder_concurrentSamePayment_onlyOneOrderCreated() throws Exception {
		// given: 실제 Redis처럼 키가 없을 때만 저장되는 락 대역
		Map<String, String> locks = new ConcurrentHashMap<>();
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
			.thenAnswer(inv -> locks.putIfAbsent(inv.getArgument(0), inv.getArgument(1)) == null);

		Users user = TestFixture.createUser("kakao_1234", "01012345678");
		Cart cart = TestFixture.createCartWithOneItem(user);
		Addresses address = TestFixture.createAddress(1L, user);
		Stores stores = TestFixture.createStore();
		ReflectionTestUtils.setField(user, "cart", cart);

		CustomOAuth2User customOAuth2User = mock(CustomOAuth2User.class);
		when(customOAuth2User.getName()).thenReturn("kakao_1234");

		OrderCreateRequest request = new OrderCreateRequest(
			1L, "요청사항", "배달메모", OrderType.DELIVERY, null,
			"paymentid", false, null, 36000, 0
		);

		// 첫 요청이 락을 잡은 채 결제 조회에서 멈춰 있도록 해 두 요청을 겹치게 함
		CountDownLatch firstEntered = new CountDownLatch(1);
		CountDownLatch releaseFirst = new CountDownLatch(1);
		when(portOneService.getPaidPayment("paymentid")).thenAnswer(inv -> {
			firstEntered.countDown();
			releaseFirst.await(5, TimeUnit.SECONDS);
			return TestFixture.createPortOnePaymentResponse("paymentid", "tx-123", 36000);
		});
		when(userService.getUser("kakao_1234")).thenReturn(user);
		when(addressRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(address));
		when(deliveryRegionService.isDeliverable(stores.getId(), address.getLatitude(), address.getLongitude()))
			.thenReturn(DeliveryRegionAbleResponse.builder().able(true).build());
		when(orderRepository.save(any())).thenReturn(Orders.builder().id(10L).build());

		ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			// when: 첫 요청이 처리 중일 때 같은 결제로 두 번째 요청
			Future<Long> first = executor.submit(() -> orderCreateService.createOrder(request, customOAuth2User));
			assertTrue(firstEntered.await(5, TimeUnit.SECONDS));

			assertThatThrownBy(() -> orderCreateService.createOrder(request, customOAuth2User))
				.isInstanceOf(InvalidPaymentException.class)
				.hasMessage("이미 처리 중이거나 사용된 결제입니다.");

			releaseFirst.countDown();

			// then
			assertThat(first.get(5, TimeUnit.SECONDS)).isEqualTo(10L);
		} finally {
			executor.shutdownNow();
		}

		verify(portOneService, times(1)).getPaidPayment("paymentid");
		verify(orderRepository, times(1)).save(any());
		verify(eventPublisher, never()).publishEvent(any(OrderCreateException.class));
	}
}