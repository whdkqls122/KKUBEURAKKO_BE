package com.kkubeurakko.kkubeurakko_be.domain.orders.transaction;

import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kkubeurakko.kkubeurakko_be.domain.orders.exception.OrderCreateException;
import com.kkubeurakko.kkubeurakko_be.domain.payments.repository.PaymentRepository;
import com.kkubeurakko.kkubeurakko_be.domain.portone.service.PortOneService;
import com.kkubeurakko.kkubeurakko_be.global.task.service.FailedTaskService;

@ExtendWith(MockitoExtension.class)
class OrderRollbackListenerTest {

	@Mock
	private PortOneService portOneService;
	@Mock
	private FailedTaskService failedTaskService;
	@Mock
	private PaymentRepository paymentRepository;
	@InjectMocks
	private OrderRollbackListener listener;

	private OrderCreateException orderCreateException(boolean cashReceipt) {
		return new OrderCreateException(
			"주문 생성 실패",
			"paymentid",
			cashReceipt,
			new IllegalStateException("배달 불가능한 지역입니다.")
		);
	}

	@Test
	@DisplayName("주문 생성이 롤백되면 결제와 현금영수증을 취소한다")
	void handleRollback_cancelPayment() throws Exception {
		// given
		given(paymentRepository.existsByPaymentId("paymentid")).willReturn(false);

		// when
		listener.handleRollback(orderCreateException(true));

		// then
		verify(portOneService).cancelPortOnePayment("paymentid");
		verify(portOneService).cancelIssueCashReceipt("paymentid");
	}

	@Test
	@DisplayName("다른 요청이 같은 결제로 주문을 커밋했다면 결제를 취소하지 않는다")
	void handleRollback_usedByCommittedOrder_noCancel() throws Exception {
		// given
		given(paymentRepository.existsByPaymentId("paymentid")).willReturn(true);

		// when
		listener.handleRollback(orderCreateException(true));

		// then
		verify(portOneService, never()).cancelPortOnePayment(anyString());
		verify(portOneService, never()).cancelIssueCashReceipt(anyString());
	}
}