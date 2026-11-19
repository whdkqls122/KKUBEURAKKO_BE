package com.kkubeurakko.kkubeurakko_be.domain.orders.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import com.kkubeurakko.kkubeurakko_be.TestFixture;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.entity.Addresses;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.repository.AddressRepository;
import com.kkubeurakko.kkubeurakko_be.domain.cart.entity.Cart;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.CouponCategory;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.CouponType;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.Coupons;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.Status;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.UsersCoupons;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.repository.UserCouponRepository;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderCreateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderCreatedEvent;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderType;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Orders;
import com.kkubeurakko.kkubeurakko_be.domain.orders.repository.OrderRepository;
import com.kkubeurakko.kkubeurakko_be.domain.orders.util.OrderNumberUtil;
import com.kkubeurakko.kkubeurakko_be.domain.payments.entity.Payments;
import com.kkubeurakko.kkubeurakko_be.domain.payments.repository.PaymentRepository;
import com.kkubeurakko.kkubeurakko_be.domain.payments.service.PaymentAttemptService;
import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.PortOnePaymentResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.DeliveryRegionAbleResponse;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;
import com.kkubeurakko.kkubeurakko_be.domain.stores.service.DeliveryRegionService;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.service.UserService;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

@ExtendWith(MockitoExtension.class)
class OrderCreateServiceImplTest {

	private static final String PAYMENT_ID = "paymentid";
	private static final int CART_TOTAL = 36000;

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
	private ApplicationEventPublisher eventPublisher;
	@Mock
	private OrderNumberUtil orderNumberUtil;
	@Mock
	private DeliveryRegionService deliveryRegionService;
	@Mock
	private PaymentAttemptService paymentAttemptService;

	@InjectMocks
	private OrderCreateServiceImpl orderCreateService;

	private Users user;
	private Addresses address;
	private Stores store;
	private CustomOAuth2User customOAuth2User;

	@BeforeEach
	void setUp() {
		user = TestFixture.createUser("kakao_1234", "01012345678");
		Cart cart = TestFixture.createCartWithOneItem(user);
		ReflectionTestUtils.setField(user, "cart", cart);
		address = TestFixture.createAddress(1L, user);
		store = TestFixture.createStore();
		customOAuth2User = mock(CustomOAuth2User.class);
	}

	private OrderCreateRequest deliveryRequest(String couponNumber) {
		return new OrderCreateRequest(
			1L, "젓가락 2개 주세요", "초인종 누르지 말아주세요", OrderType.DELIVERY, couponNumber,
			PAYMENT_ID, false, null, CART_TOTAL, 0
		);
	}

	private PortOnePaymentResponseDto paidPayment(int amount) {
		return TestFixture.createPortOnePaymentResponse(PAYMENT_ID, "tx-123", amount);
	}

	/** 로그인한 사용자의 장바구니가 있고, 주소가 배달 가능 지역인 상태 */
	private void givenDeliverableUser() {
		given(customOAuth2User.getName()).willReturn("kakao_1234");
		given(userService.getUser("kakao_1234")).willReturn(user);
		given(addressRepository.findByIdAndUser(1L, user)).willReturn(Optional.of(address));
		given(deliveryRegionService.isDeliverable(store.getId(), address.getLatitude(), address.getLongitude()))
			.willReturn(DeliveryRegionAbleResponse.builder().able(true).build());
	}

	@Test
	@DisplayName("주문과 결제를 저장하고 결제 처리 기록을 완료로 바꾼 뒤 후처리를 한다")
	void createOrder() {
		// given
		givenDeliverableUser();
		ArgumentCaptor<Orders> orderCaptor = ArgumentCaptor.forClass(Orders.class);
		ArgumentCaptor<Payments> paymentCaptor = ArgumentCaptor.forClass(Payments.class);
		given(orderRepository.save(orderCaptor.capture())).willReturn(Orders.builder().id(10L).build());

		// when
		Long orderId = orderCreateService.createOrder(deliveryRequest(null), customOAuth2User, paidPayment(CART_TOTAL));

		// then
		assertThat(orderId).isEqualTo(10L);
		verify(paymentRepository).save(paymentCaptor.capture());
		assertThat(paymentCaptor.getValue().getPaymentId()).isEqualTo(PAYMENT_ID);
		assertThat(orderCaptor.getValue().getPayments()).isSameAs(paymentCaptor.getValue());
		verify(paymentAttemptService).complete(PAYMENT_ID);

		// 주문 후처리: 장바구니 비우기, 주소 사용 시각 갱신, 주문 생성 이벤트 발행
		assertThat(user.getCart()).isNull();
		assertThat(address.getLastUsedAt()).isNotNull();
		ArgumentCaptor<OrderCreatedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
		verify(eventPublisher).publishEvent(eventCaptor.capture());
		assertThat(eventCaptor.getValue().orderId()).isEqualTo(10L);
		assertThat(eventCaptor.getValue().storeId()).isEqualTo(store.getId());
	}

	@Test
	@DisplayName("주문번호를 생성해 주문에 저장한다")
	void createOrder_withOrderNumber() {
		// given
		givenDeliverableUser();
		given(orderNumberUtil.createOrderNumber(anyLong(), eq(OrderType.DELIVERY))).willReturn("A-1");
		ArgumentCaptor<Orders> captor = ArgumentCaptor.forClass(Orders.class);
		given(orderRepository.save(captor.capture())).willReturn(Orders.builder().id(10L).build());

		// when
		orderCreateService.createOrder(deliveryRequest(null), customOAuth2User, paidPayment(CART_TOTAL));

		// then
		assertThat(captor.getValue().getOrderNumber()).isEqualTo("A-1");
	}

	@Test
	@DisplayName("쿠폰을 사용하면 할인된 금액으로 결제를 검증하고 쿠폰을 사용 처리한다")
	void createOrder_withCoupon() {
		// given: 1,000원 할인 쿠폰 (최소 주문 12,000원)
		givenDeliverableUser();
		Coupons coupon = TestFixture.createCoupon(1L, "할인 쿠폰", 10, LocalDate.now().minusDays(1),
			LocalDate.now().plusDays(1), CouponType.ALL, CouponCategory.GENERAL, Status.OFFER, store);
		UsersCoupons usersCoupon = TestFixture.createUserCoupon(1L, user, coupon, false);
		String couponNumber = usersCoupon.getCouponsNumber();
		given(userCouponRepository.findByUser_UserNumberAndCouponsNumber(user.getUserNumber(), couponNumber))
			.willReturn(Optional.of(usersCoupon));
		ArgumentCaptor<Payments> paymentCaptor = ArgumentCaptor.forClass(Payments.class);
		given(orderRepository.save(any())).willReturn(Orders.builder().id(10L).build());

		// when
		orderCreateService.createOrder(deliveryRequest(couponNumber), customOAuth2User, paidPayment(CART_TOTAL - 1000));

		// then
		verify(paymentRepository).save(paymentCaptor.capture());
		assertThat(paymentCaptor.getValue().getDiscountAmount()).isEqualTo(1000);
		assertThat(paymentCaptor.getValue().getFinalAmount()).isEqualTo(CART_TOTAL - 1000);
		assertThat(usersCoupon.isUsed()).isTrue();
	}

	@Test
	@DisplayName("결제 금액이 장바구니 금액과 다르면 주문을 저장하지 않고 예외를 던진다")
	void createOrder_amountMismatch() {
		// given: 장바구니 금액은 36,000원인데 결제는 30,000원
		givenDeliverableUser();

		// when & then
		assertThatThrownBy(() -> orderCreateService.createOrder(deliveryRequest(null), customOAuth2User, paidPayment(30000)))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("결제 금액이 일치하지 않습니다.");

		verify(orderRepository, never()).save(any());
		verify(paymentAttemptService, never()).complete(anyString());
		verify(eventPublisher, never()).publishEvent(any(Object.class));
	}

	@Test
	@DisplayName("장바구니가 없으면 주문을 저장하지 않고 예외를 던진다")
	void createOrder_emptyCart() {
		// given
		Users userWithoutCart = TestFixture.createUser("kakao_5678", "01056785678");
		given(customOAuth2User.getName()).willReturn("kakao_5678");
		given(userService.getUser("kakao_5678")).willReturn(userWithoutCart);

		// when & then
		assertThatThrownBy(() -> orderCreateService.createOrder(deliveryRequest(null), customOAuth2User, paidPayment(CART_TOTAL)))
			.isInstanceOf(IllegalStateException.class);

		verify(orderRepository, never()).save(any());
		verify(paymentAttemptService, never()).complete(anyString());
	}

	@Test
	@DisplayName("결제가 이미 취소 요청됐으면 결제 처리 기록 완료에서 예외가 나 주문 트랜잭션이 롤백된다")
	void createOrder_alreadyCancelRequested() {
		// given
		givenDeliverableUser();
		given(orderRepository.save(any())).willReturn(Orders.builder().id(10L).build());
		willThrow(new IllegalStateException("결제 처리 상태가 변경되어 주문을 완료할 수 없습니다."))
			.given(paymentAttemptService).complete(PAYMENT_ID);

		// when & then
		assertThatThrownBy(() -> orderCreateService.createOrder(deliveryRequest(null), customOAuth2User, paidPayment(CART_TOTAL)))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("결제 처리 상태가 변경되어 주문을 완료할 수 없습니다.");
	}
}