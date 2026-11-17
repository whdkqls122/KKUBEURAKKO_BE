package com.kkubeurakko.kkubeurakko_be.global.task.service;

import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkubeurakko.kkubeurakko_be.domain.payments.entity.PaymentAttemptStatus;
import com.kkubeurakko.kkubeurakko_be.domain.payments.repository.PaymentRepository;
import com.kkubeurakko.kkubeurakko_be.domain.payments.service.PaymentAttemptService;
import com.kkubeurakko.kkubeurakko_be.domain.portone.service.PortOneService;
import com.kkubeurakko.kkubeurakko_be.global.oauth.service.OAuthUnlinkService;
import com.kkubeurakko.kkubeurakko_be.global.task.entity.FailedTask;
import com.kkubeurakko.kkubeurakko_be.global.task.entity.TaskStatus;
import com.kkubeurakko.kkubeurakko_be.global.task.entity.TaskType;

@ExtendWith(MockitoExtension.class)
class TaskProcessorImplTest {

	@Mock
	private OAuthUnlinkService oAuthUnlinkService;
	@Mock
	private PortOneService portOneService;
	@Mock
	private PaymentRepository paymentRepository;
	@Mock
	private PaymentAttemptService paymentAttemptService;
	@Spy
	private ObjectMapper objectMapper = new ObjectMapper();
	@InjectMocks
	private TaskProcessorImpl taskProcessor;

	private FailedTask paymentTask(TaskType taskType) {
		return FailedTask.builder()
			.taskType(taskType)
			.payload("{\"paymentId\":\"paymentid\"}")
			.retryCount(0)
			.taskStatus(TaskStatus.PENDING)
			.build();
	}

	@Test
	@DisplayName("결제 취소 작업은 다른 주문에 사용되지 않은 결제면 결제를 취소한다")
	void processRefundPayment() throws Exception {
		// given
		given(paymentRepository.existsByPaymentId("paymentid")).willReturn(false);

		// when
		taskProcessor.process(paymentTask(TaskType.REFUND_PAYMENT));

		// then
		verify(portOneService).retryCancelPortOnePayment("paymentid");
	}

	@Test
	@DisplayName("결제 취소 작업은 다른 주문에 사용된 결제면 취소하지 않는다")
	void processRefundPayment_usedByCommittedOrder() throws Exception {
		// given
		given(paymentRepository.existsByPaymentId("paymentid")).willReturn(true);

		// when
		taskProcessor.process(paymentTask(TaskType.REFUND_PAYMENT));

		// then
		verify(portOneService, never()).retryCancelPortOnePayment(anyString());
	}

	@Test
	@DisplayName("현금영수증 취소 작업은 다른 주문에 사용되지 않은 결제면 현금영수증을 취소한다")
	void processCashReceiptCancel() throws Exception {
		// given
		given(paymentRepository.existsByPaymentId("paymentid")).willReturn(false);

		// when
		taskProcessor.process(paymentTask(TaskType.CANCEL_CASH_RECEIPT));

		// then
		verify(portOneService).retryCancelIssueCashReceipt("paymentid");
	}

	@Test
	@DisplayName("현금영수증 취소 작업은 다른 주문에 사용된 결제면 취소하지 않는다")
	void processCashReceiptCancel_usedByCommittedOrder() throws Exception {
		// given
		given(paymentRepository.existsByPaymentId("paymentid")).willReturn(true);

		// when
		taskProcessor.process(paymentTask(TaskType.CANCEL_CASH_RECEIPT));

		// then
		verify(portOneService, never()).retryCancelIssueCashReceipt(anyString());
	}

	@Test
	@DisplayName("결제 처리 기록이 취소 요청 상태면 결제를 취소한다")
	void processRefundPayment_cancelRequested() throws Exception {
		// given
		given(paymentAttemptService.findStatus("paymentid")).willReturn(Optional.of(PaymentAttemptStatus.CANCEL_REQUESTED));

		// when
		taskProcessor.process(paymentTask(TaskType.REFUND_PAYMENT));

		// then
		verify(portOneService).retryCancelPortOnePayment("paymentid");
		verify(paymentRepository, never()).existsByPaymentId(anyString());
	}

	@Test
	@DisplayName("결제 처리 기록이 완료 상태면 주문에 사용된 결제이므로 결제와 현금영수증을 취소하지 않는다")
	void process_completedAttempt_noCancel() throws Exception {
		// given
		given(paymentAttemptService.findStatus("paymentid")).willReturn(Optional.of(PaymentAttemptStatus.COMPLETED));

		// when
		taskProcessor.process(paymentTask(TaskType.REFUND_PAYMENT));
		taskProcessor.process(paymentTask(TaskType.CANCEL_CASH_RECEIPT));

		// then
		verify(portOneService, never()).retryCancelPortOnePayment(anyString());
		verify(portOneService, never()).retryCancelIssueCashReceipt(anyString());
	}
}