package com.kkubeurakko.kkubeurakko_be.global.task.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkubeurakko.kkubeurakko_be.domain.payments.entity.PaymentAttemptStatus;
import com.kkubeurakko.kkubeurakko_be.domain.payments.exception.InvalidPaymentException;
import com.kkubeurakko.kkubeurakko_be.domain.payments.repository.PaymentRepository;
import com.kkubeurakko.kkubeurakko_be.domain.payments.service.PaymentAttemptService;
import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.PortOnePaymentResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.portone.exception.PortOnePaymentHistoryException;
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

	private PortOnePaymentResponseDto ownPayment(String status) {
		return new PortOnePaymentResponseDto(status, "paymentid", "tx-123", "store-test-001", null, null);
	}

	private void givenCancelRequested() {
		given(paymentAttemptService.findStatus("paymentid"))
			.willReturn(Optional.of(PaymentAttemptStatus.CANCEL_REQUESTED));
	}

	@Test
	@DisplayName("처리 기록이 없는 이전 작업은 커밋된 주문이 없고 우리 상점의 완료된 결제면 취소한다")
	void processRefundPayment_legacyTask() throws Exception {
		// given
		given(paymentRepository.existsByPaymentId("paymentid")).willReturn(false);
		given(portOneService.getOwnPayment("paymentid")).willReturn(ownPayment("PAID"));

		// when
		taskProcessor.process(paymentTask(TaskType.REFUND_PAYMENT));

		// then
		verify(portOneService).retryCancelPortOnePayment("paymentid");
	}

	@Test
	@DisplayName("처리 기록이 없는 이전 작업은 커밋된 주문에 사용된 결제면 결제와 현금영수증을 취소하지 않는다")
	void process_legacyTask_usedByCommittedOrder() throws Exception {
		// given
		given(paymentRepository.existsByPaymentId("paymentid")).willReturn(true);

		// when
		taskProcessor.process(paymentTask(TaskType.REFUND_PAYMENT));
		taskProcessor.process(paymentTask(TaskType.CANCEL_CASH_RECEIPT));

		// then
		verify(portOneService, never()).getOwnPayment(anyString());
		verify(portOneService, never()).retryCancelPortOnePayment(anyString());
		verify(portOneService, never()).retryCancelIssueCashReceipt(anyString());
	}

	@Test
	@DisplayName("결제 처리 기록이 취소 요청 상태이고 우리 상점의 완료된 결제면 결제를 취소한다")
	void processRefundPayment_cancelRequested() throws Exception {
		// given
		givenCancelRequested();
		given(portOneService.getOwnPayment("paymentid")).willReturn(ownPayment("PAID"));

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
		verify(portOneService, never()).getOwnPayment(anyString());
		verify(portOneService, never()).retryCancelPortOnePayment(anyString());
		verify(portOneService, never()).retryCancelIssueCashReceipt(anyString());
	}

	@Test
	@DisplayName("이미 취소됐거나 완료되지 않은 결제면 결제 취소를 건너뛴다")
	void processRefundPayment_notPaid_noCancel() throws Exception {
		// given
		givenCancelRequested();
		given(portOneService.getOwnPayment("paymentid")).willReturn(ownPayment("CANCELLED"));

		// when
		taskProcessor.process(paymentTask(TaskType.REFUND_PAYMENT));

		// then
		verify(portOneService, never()).retryCancelPortOnePayment(anyString());
	}

	@Test
	@DisplayName("우리 상점 결제로 확인되지 않으면 결제와 현금영수증을 취소하지 않는다")
	void process_notOwnPayment_noCancel() throws Exception {
		// given
		givenCancelRequested();
		given(portOneService.getOwnPayment("paymentid"))
			.willThrow(new InvalidPaymentException("결제 상점 정보가 일치하지 않습니다."));

		// when
		taskProcessor.process(paymentTask(TaskType.REFUND_PAYMENT));
		taskProcessor.process(paymentTask(TaskType.CANCEL_CASH_RECEIPT));

		// then
		verify(portOneService, never()).retryCancelPortOnePayment(anyString());
		verify(portOneService, never()).retryCancelIssueCashReceipt(anyString());
	}

	@Test
	@DisplayName("PortOne 결제 조회에 실패하면 취소하지 않고 예외를 던져 다음 주기에 재시도한다")
	void processRefundPayment_lookupFailed_retry() throws JsonProcessingException {
		// given
		givenCancelRequested();
		given(portOneService.getOwnPayment("paymentid"))
			.willThrow(new PortOnePaymentHistoryException("PortOne 결제 조회 실패: ", new RuntimeException("timeout")));

		// when & then
		assertThatThrownBy(() -> taskProcessor.process(paymentTask(TaskType.REFUND_PAYMENT)))
			.isInstanceOf(PortOnePaymentHistoryException.class);
		verify(portOneService, never()).retryCancelPortOnePayment(anyString());
	}

	@Test
	@DisplayName("결제가 먼저 취소돼 CANCELLED여도 우리 상점 결제면 현금영수증은 취소한다")
	void processCashReceiptCancel_paymentAlreadyCancelled() throws Exception {
		// given
		givenCancelRequested();
		given(portOneService.getOwnPayment("paymentid")).willReturn(ownPayment("CANCELLED"));

		// when
		taskProcessor.process(paymentTask(TaskType.CANCEL_CASH_RECEIPT));

		// then
		verify(portOneService).retryCancelIssueCashReceipt("paymentid");
	}
}