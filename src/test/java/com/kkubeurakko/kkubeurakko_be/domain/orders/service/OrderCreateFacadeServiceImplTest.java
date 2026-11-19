package com.kkubeurakko.kkubeurakko_be.domain.orders.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;

import com.kkubeurakko.kkubeurakko_be.TestFixture;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.CashReceiptRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderCreateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderType;
import com.kkubeurakko.kkubeurakko_be.domain.orders.exception.OrderCreateException;
import com.kkubeurakko.kkubeurakko_be.domain.payments.exception.InvalidPaymentException;
import com.kkubeurakko.kkubeurakko_be.domain.payments.repository.PaymentRepository;
import com.kkubeurakko.kkubeurakko_be.domain.payments.service.PaymentAttemptService;
import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.CashReceiptType;
import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.PortOnePaymentResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.portone.exception.PortOnePaymentHistoryException;
import com.kkubeurakko.kkubeurakko_be.domain.portone.service.PortOneService;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.service.UserService;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

@ExtendWith(MockitoExtension.class)
class OrderCreateFacadeServiceImplTest {

	private static final String PAYMENT_ID = "paymentid";
	private static final int PAID_AMOUNT = 36000;

	@Mock
	private OrderCreateService orderCreateService;
	@Mock
	private PaymentRepository paymentRepository;
	@Mock
	private PaymentAttemptService paymentAttemptService;
	@Mock
	private PortOneService portOneService;
	@Mock
	private UserService userService;

	@InjectMocks
	private OrderCreateFacadeServiceImpl orderCreateFacadeService;

	private CustomOAuth2User customOAuth2User;
	private PortOnePaymentResponseDto paidPayment;

	@BeforeEach
	void setUp() {
		customOAuth2User = mock(CustomOAuth2User.class);
		paidPayment = TestFixture.createPortOnePaymentResponse(PAYMENT_ID, "tx-123", PAID_AMOUNT);
	}

	private OrderCreateRequest request(boolean cashReceipt, CashReceiptRequestDto cashReceiptRequest) {
		return new OrderCreateRequest(
			1L, "요청사항", "배달메모", OrderType.DELIVERY, null,
			PAYMENT_ID, cashReceipt, cashReceiptRequest, PAID_AMOUNT, 0
		);
	}

	private OrderCreateRequest request() {
		return request(false, null);
	}

	private void givenPaidPayment() {
		given(portOneService.getPaidPayment(PAYMENT_ID)).willReturn(paidPayment);
	}

	// 결제 확인 이후 단계

	@Test
	@DisplayName("결제를 확인한 뒤 주문 저장을 맡기고, 성공하면 결제 취소를 요청하지 않는다")
	void createOrder() {
		// given
		givenPaidPayment();
		OrderCreateRequest request = request();
		given(orderCreateService.createOrder(request, customOAuth2User, paidPayment)).willReturn(10L);

		// when
		Long orderId = orderCreateFacadeService.createOrder(request, customOAuth2User);

		// then
		assertThat(orderId).isEqualTo(10L);
		verify(paymentAttemptService).start(PAYMENT_ID, false);
		verify(portOneService, never()).issueCashReceipt(any(), anyString(), any(), anyInt());
		verify(paymentAttemptService, never()).requestCancel(anyString(), anyString(), any());
	}

	@Test
	@DisplayName("현금영수증을 요청하면 주문 저장 전에 결제된 금액으로 발급한다")
	void createOrder_issueCashReceiptBeforeSave() {
		// given
		givenPaidPayment();
		CashReceiptRequestDto cashReceiptRequest = new CashReceiptRequestDto(CashReceiptType.PERSONAL, "01012345678");
		OrderCreateRequest request = request(true, cashReceiptRequest);
		Users user = TestFixture.createUser("kakao_1234", "01012345678");
		given(customOAuth2User.getName()).willReturn("kakao_1234");
		given(userService.getUser("kakao_1234")).willReturn(user);
		given(orderCreateService.createOrder(request, customOAuth2User, paidPayment)).willReturn(10L);

		// when
		orderCreateFacadeService.createOrder(request, customOAuth2User);

		// then
		InOrder inOrder = inOrder(portOneService, orderCreateService);
		inOrder.verify(portOneService).issueCashReceipt(user, PAYMENT_ID, cashReceiptRequest, PAID_AMOUNT);
		inOrder.verify(orderCreateService).createOrder(request, customOAuth2User, paidPayment);
	}

	@Test
	@DisplayName("주문 저장 단계에서 실패하면 실패 원인과 함께 결제 취소를 바로 요청하고 사용자 안내 메시지로 감싼다")
	void createOrder_saveFailed_requestCancel() {
		// given
		givenPaidPayment();
		OrderCreateRequest request = request();
		given(orderCreateService.createOrder(request, customOAuth2User, paidPayment))
			.willThrow(new IllegalStateException("배달 불가능한 지역입니다."));

		// when & then
		assertThatThrownBy(() -> orderCreateFacadeService.createOrder(request, customOAuth2User))
			.isInstanceOf(OrderCreateException.class)
			.hasMessageContaining("배달 불가능한 지역입니다.")
			.hasMessageContaining("잠시 후 자동으로 취소됩니다");

		verify(paymentAttemptService).requestCancel(PAYMENT_ID, "IllegalStateException", "배달 불가능한 지역입니다.");
	}

	@Test
	@DisplayName("커밋 시점에 실패해도 결제 취소를 요청하고, DB 오류 메시지는 사용자에게 노출하지 않는다")
	void createOrder_commitFailed_requestCancel() {
		// given: 서비스 메서드가 끝나며 커밋할 때 제약 조건 위반이 난 상황
		givenPaidPayment();
		OrderCreateRequest request = request();
		given(orderCreateService.createOrder(request, customOAuth2User, paidPayment))
			.willThrow(new DataIntegrityViolationException("Duplicate entry 'paymentid' for key 'payments.payment_id'"));

		// when & then
		assertThatThrownBy(() -> orderCreateFacadeService.createOrder(request, customOAuth2User))
			.isInstanceOf(OrderCreateException.class)
			.hasMessageContaining("주문을 처리하는 중 오류가 발생했습니다.")
			.hasMessageNotContaining("Duplicate entry");

		verify(paymentAttemptService).requestCancel(eq(PAYMENT_ID), eq("DataIntegrityViolationException"), anyString());
	}

	@Test
	@DisplayName("현금영수증 발급에 실패하면 주문을 저장하지 않고 결제 취소를 요청한다")
	void createOrder_cashReceiptFailed_requestCancel() {
		// given
		givenPaidPayment();
		OrderCreateRequest request = request(true, new CashReceiptRequestDto(CashReceiptType.PERSONAL, "01012345678"));
		given(customOAuth2User.getName()).willReturn("kakao_1234");
		given(userService.getUser("kakao_1234")).willReturn(TestFixture.createUser("kakao_1234", "01012345678"));
		willThrow(new IllegalStateException("현금영수증 발급에 실패했습니다."))
			.given(portOneService).issueCashReceipt(any(), anyString(), any(), anyInt());

		// when & then
		assertThatThrownBy(() -> orderCreateFacadeService.createOrder(request, customOAuth2User))
			.isInstanceOf(OrderCreateException.class);

		verify(orderCreateService, never()).createOrder(any(), any(), any());
		verify(paymentAttemptService).requestCancel(PAYMENT_ID, "IllegalStateException", "현금영수증 발급에 실패했습니다.");
	}

	@Test
	@DisplayName("결제 취소 요청에 실패해도 주문 실패 예외를 던진다 (방치 감지 스케줄러가 다시 처리)")
	void createOrder_requestCancelFailed() {
		// given
		givenPaidPayment();
		OrderCreateRequest request = request();
		given(orderCreateService.createOrder(request, customOAuth2User, paidPayment))
			.willThrow(new IllegalStateException("배달 불가능한 지역입니다."));
		willThrow(new DataAccessResourceFailureException("DB 연결 실패"))
			.given(paymentAttemptService).requestCancel(anyString(), anyString(), any());

		// when & then
		assertThatThrownBy(() -> orderCreateFacadeService.createOrder(request, customOAuth2User))
			.isInstanceOf(OrderCreateException.class)
			.hasMessageContaining("배달 불가능한 지역입니다.");
	}

	// 결제 확인 전 단계 (결제 취소를 요청하지 않음)

	@Test
	@DisplayName("사용할 수 없는 결제면 처리 기록을 지우고 주문 저장과 결제 취소 요청 없이 거절한다")
	void createOrder_invalidPayment() {
		// given
		given(portOneService.getPaidPayment(PAYMENT_ID))
			.willThrow(new InvalidPaymentException("결제가 완료되지 않았습니다."));

		// when & then
		assertThatThrownBy(() -> orderCreateFacadeService.createOrder(request(), customOAuth2User))
			.isInstanceOf(InvalidPaymentException.class);

		verify(paymentAttemptService).discard(PAYMENT_ID);
		verify(orderCreateService, never()).createOrder(any(), any(), any());
		verify(paymentAttemptService, never()).requestCancel(anyString(), anyString(), any());
	}

	@Test
	@DisplayName("이미 사용된 결제면 처리 기록과 결제 조회 없이 거절한다")
	void createOrder_duplicatePayment() {
		// given
		given(paymentRepository.existsByPaymentId(PAYMENT_ID)).willReturn(true);

		// when & then
		assertThatThrownBy(() -> orderCreateFacadeService.createOrder(request(), customOAuth2User))
			.isInstanceOf(InvalidPaymentException.class)
			.hasMessage("이미 사용된 결제입니다.");

		verify(paymentAttemptService, never()).start(anyString(), anyBoolean());
		verify(portOneService, never()).getPaidPayment(anyString());
	}

	@Test
	@DisplayName("같은 결제가 이미 처리 중이면 결제 조회 없이 거절한다")
	void createOrder_paymentInProgress() {
		// given
		willThrow(new InvalidPaymentException("이미 처리 중이거나 사용된 결제입니다."))
			.given(paymentAttemptService).start(PAYMENT_ID, false);

		// when & then
		assertThatThrownBy(() -> orderCreateFacadeService.createOrder(request(), customOAuth2User))
			.isInstanceOf(InvalidPaymentException.class)
			.hasMessage("이미 처리 중이거나 사용된 결제입니다.");

		verify(portOneService, never()).getPaidPayment(anyString());
	}

	@Test
	@DisplayName("PortOne 결제 조회에 실패하면 처리 기록을 남기고 자동 취소와 재결제를 안내한다")
	void createOrder_paymentLookupFailed() {
		// given
		given(portOneService.getPaidPayment(PAYMENT_ID))
			.willThrow(new PortOnePaymentHistoryException("PortOne 결제 조회 실패: ", new RuntimeException("timeout")));

		// when & then
		assertThatThrownBy(() -> orderCreateFacadeService.createOrder(request(), customOAuth2User))
			.isInstanceOf(PortOnePaymentHistoryException.class)
			.hasMessageContaining("다시 결제해 주세요");

		verify(paymentAttemptService, never()).discard(anyString());
		verify(paymentAttemptService, never()).requestCancel(anyString(), anyString(), any());
		verify(orderCreateService, never()).createOrder(any(), any(), any());
	}

	@Test
	@DisplayName("결제 처리 기록 저장 중 DB 오류가 나면 결제 조회 없이 실패한다")
	void createOrder_attemptStoreError() {
		// given
		willThrow(new DataAccessResourceFailureException("DB 연결 실패"))
			.given(paymentAttemptService).start(PAYMENT_ID, false);

		// when & then
		assertThatThrownBy(() -> orderCreateFacadeService.createOrder(request(), customOAuth2User))
			.isInstanceOf(DataAccessResourceFailureException.class);

		verify(portOneService, never()).getPaidPayment(anyString());
	}

	@Test
	@DisplayName("같은 결제로 두 요청이 동시에 들어오면 먼저 들어온 요청만 주문을 저장하고 나머지는 결제 취소 없이 거절한다")
	void createOrder_concurrentSamePayment() throws Exception {
		// given: 실제 DB의 paymentId unique 제약처럼 같은 결제는 한 번만 기록되는 처리 기록 대역
		Set<String> startedPayments = ConcurrentHashMap.newKeySet();
		willAnswer(inv -> {
			if (!startedPayments.add(inv.getArgument(0))) {
				throw new InvalidPaymentException("이미 처리 중이거나 사용된 결제입니다.");
			}
			return null;
		}).given(paymentAttemptService).start(anyString(), anyBoolean());

		// 첫 요청이 처리 기록을 남긴 채 결제 조회에서 멈춰 있도록 해 두 요청을 겹치게 함
		CountDownLatch firstEntered = new CountDownLatch(1);
		CountDownLatch releaseFirst = new CountDownLatch(1);
		given(portOneService.getPaidPayment(PAYMENT_ID)).willAnswer(inv -> {
			firstEntered.countDown();
			releaseFirst.await(5, TimeUnit.SECONDS);
			return paidPayment;
		});
		OrderCreateRequest request = request();
		given(orderCreateService.createOrder(request, customOAuth2User, paidPayment)).willReturn(10L);

		ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			// when: 첫 요청이 처리 중일 때 같은 결제로 두 번째 요청
			Future<Long> first = executor.submit(() -> orderCreateFacadeService.createOrder(request, customOAuth2User));
			assertTrue(firstEntered.await(5, TimeUnit.SECONDS));

			assertThatThrownBy(() -> orderCreateFacadeService.createOrder(request, customOAuth2User))
				.isInstanceOf(InvalidPaymentException.class)
				.hasMessage("이미 처리 중이거나 사용된 결제입니다.");

			releaseFirst.countDown();

			// then
			assertThat(first.get(5, TimeUnit.SECONDS)).isEqualTo(10L);
		} finally {
			executor.shutdownNow();
		}

		verify(portOneService, times(1)).getPaidPayment(PAYMENT_ID);
		verify(orderCreateService, times(1)).createOrder(any(), any(), any());
		verify(paymentAttemptService, never()).requestCancel(anyString(), anyString(), any());
	}
}