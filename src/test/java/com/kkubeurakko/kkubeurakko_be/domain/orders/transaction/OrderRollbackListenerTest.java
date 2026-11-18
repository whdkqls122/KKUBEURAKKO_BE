package com.kkubeurakko.kkubeurakko_be.domain.orders.transaction;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kkubeurakko.kkubeurakko_be.domain.orders.exception.OrderCreateException;
import com.kkubeurakko.kkubeurakko_be.domain.payments.service.PaymentAttemptService;

@ExtendWith(MockitoExtension.class)
class OrderRollbackListenerTest {

	@Mock
	private PaymentAttemptService paymentAttemptService;
	@InjectMocks
	private OrderRollbackListener listener;

	private OrderCreateException orderCreateException() {
		return new OrderCreateException(
			"주문 생성 실패",
			"paymentid",
			true,
			new IllegalStateException("배달 불가능한 지역입니다.")
		);
	}

	@Test
	@DisplayName("주문 생성이 롤백되면 실패 원인과 함께 결제 취소를 요청한다")
	void handleRollback_requestCancel() {
		// when
		listener.handleRollback(orderCreateException());

		// then
		verify(paymentAttemptService).requestCancel("paymentid", "IllegalStateException", "배달 불가능한 지역입니다.");
	}

	@Test
	@DisplayName("결제 취소 요청에 실패해도 예외를 밖으로 던지지 않는다 (방치 감지 스케줄러가 다시 처리)")
	void handleRollback_requestCancelFailed() {
		// given
		willThrow(new RuntimeException("DB 연결 실패"))
			.given(paymentAttemptService).requestCancel(anyString(), anyString(), anyString());

		// when & then
		assertThatCode(() -> listener.handleRollback(orderCreateException()))
			.doesNotThrowAnyException();
	}
}