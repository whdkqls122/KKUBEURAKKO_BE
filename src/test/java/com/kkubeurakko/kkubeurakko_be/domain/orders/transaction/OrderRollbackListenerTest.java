package com.kkubeurakko.kkubeurakko_be.domain.orders.transaction;

import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kkubeurakko.kkubeurakko_be.domain.orders.exception.OrderCreateException;
import com.kkubeurakko.kkubeurakko_be.domain.portone.service.PortOneService;
import com.kkubeurakko.kkubeurakko_be.global.task.service.FailedTaskService;

@ExtendWith(MockitoExtension.class)
class OrderRollbackListenerTest {

	@Mock
	private PortOneService portOneService;
	@Mock
	private FailedTaskService failedTaskService;
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
	@DisplayName("주문 생성이 롤백되면 결제를 바로 취소하지 않고 원인과 함께 취소 작업을 등록한다")
	void handleRollback_registerTasks() throws Exception {
		// when
		listener.handleRollback(orderCreateException(true));

		// then
		verify(failedTaskService).saveRefundPaymentTask("paymentid", "IllegalStateException", "배달 불가능한 지역입니다.");
		verify(failedTaskService).saveCashReceiptCancelTask("paymentid", "IllegalStateException", "배달 불가능한 지역입니다.");
		verify(portOneService, never()).cancelPortOnePayment(anyString());
		verify(portOneService, never()).cancelIssueCashReceipt(anyString());
	}

	@Test
	@DisplayName("현금영수증을 요청하지 않았으면 현금영수증 취소 작업은 등록하지 않는다")
	void handleRollback_noCashReceipt() throws Exception {
		// when
		listener.handleRollback(orderCreateException(false));

		// then
		verify(failedTaskService).saveRefundPaymentTask("paymentid", "IllegalStateException", "배달 불가능한 지역입니다.");
		verify(failedTaskService, never()).saveCashReceiptCancelTask(anyString(), anyString(), anyString());
	}

	@Test
	@DisplayName("취소 작업 등록에 실패하면 결제를 즉시 취소한다")
	void handleRollback_registerFailed_cancelNow() throws Exception {
		// given
		willThrow(new RuntimeException("DB 연결 실패"))
			.given(failedTaskService).saveRefundPaymentTask(anyString(), anyString(), anyString());

		// when
		listener.handleRollback(orderCreateException(false));

		// then
		verify(portOneService).cancelPortOnePayment("paymentid");
	}
}