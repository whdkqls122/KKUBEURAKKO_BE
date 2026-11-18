package com.kkubeurakko.kkubeurakko_be.domain.payments.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.kkubeurakko.kkubeurakko_be.domain.payments.entity.PaymentAttempt;
import com.kkubeurakko.kkubeurakko_be.domain.payments.entity.PaymentAttemptStatus;
import com.kkubeurakko.kkubeurakko_be.domain.payments.exception.InvalidPaymentException;
import com.kkubeurakko.kkubeurakko_be.domain.payments.repository.PaymentAttemptRepository;
import com.kkubeurakko.kkubeurakko_be.global.task.service.FailedTaskService;

@ExtendWith(MockitoExtension.class)
class PaymentAttemptServiceImplTest {

	@Mock
	private PaymentAttemptRepository paymentAttemptRepository;
	@Mock
	private FailedTaskService failedTaskService;
	@InjectMocks
	private PaymentAttemptServiceImpl paymentAttemptService;

	@Test
	@DisplayName("같은 결제로 이미 처리 기록이 있으면 이미 처리 중인 결제로 거절한다")
	void start_duplicate() {
		// given
		given(paymentAttemptRepository.saveAndFlush(any(PaymentAttempt.class)))
			.willThrow(new DataIntegrityViolationException("Duplicate entry"));

		// when & then
		assertThatThrownBy(() -> paymentAttemptService.start("paymentid", false))
			.isInstanceOf(InvalidPaymentException.class)
			.hasMessage("이미 처리 중이거나 사용된 결제입니다.");
	}

	@Test
	@DisplayName("처리 중인 결제를 주문과 함께 완료한다")
	void complete() {
		// given
		given(paymentAttemptRepository.changeStatus(
			"paymentid", PaymentAttemptStatus.PROCESSING, PaymentAttemptStatus.COMPLETED)).willReturn(1);

		// when & then
		assertThatCode(() -> paymentAttemptService.complete("paymentid")).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("이미 취소 요청된 결제면 주문을 완료하지 못하고 예외로 롤백시킨다")
	void complete_alreadyCancelRequested() {
		// given
		given(paymentAttemptRepository.changeStatus(
			"paymentid", PaymentAttemptStatus.PROCESSING, PaymentAttemptStatus.COMPLETED)).willReturn(0);

		// when & then
		assertThatThrownBy(() -> paymentAttemptService.complete("paymentid"))
			.isInstanceOf(IllegalStateException.class);
	}

	@Test
	@DisplayName("처리 중인 결제를 취소 요청 상태로 바꾸고 결제·현금영수증 취소 작업을 등록한다")
	void requestCancel() throws Exception {
		// given
		given(paymentAttemptRepository.changeStatus(
			"paymentid", PaymentAttemptStatus.PROCESSING, PaymentAttemptStatus.CANCEL_REQUESTED)).willReturn(1);
		given(paymentAttemptRepository.findByPaymentId("paymentid"))
			.willReturn(Optional.of(PaymentAttempt.start("paymentid", true)));

		// when
		boolean requested = paymentAttemptService.requestCancel("paymentid", "IllegalStateException", "배달 불가");

		// then
		assertThat(requested).isTrue();
		verify(failedTaskService).saveRefundPaymentTask("paymentid", "IllegalStateException", "배달 불가");
		verify(failedTaskService).saveCashReceiptCancelTask("paymentid", "IllegalStateException", "배달 불가");
	}

	@Test
	@DisplayName("현금영수증을 요청하지 않은 결제면 결제 취소 작업만 등록한다")
	void requestCancel_noCashReceipt() throws Exception {
		// given
		given(paymentAttemptRepository.changeStatus(
			"paymentid", PaymentAttemptStatus.PROCESSING, PaymentAttemptStatus.CANCEL_REQUESTED)).willReturn(1);
		given(paymentAttemptRepository.findByPaymentId("paymentid"))
			.willReturn(Optional.of(PaymentAttempt.start("paymentid", false)));

		// when
		paymentAttemptService.requestCancel("paymentid", "IllegalStateException", "배달 불가");

		// then
		verify(failedTaskService).saveRefundPaymentTask("paymentid", "IllegalStateException", "배달 불가");
		verify(failedTaskService, never()).saveCashReceiptCancelTask(anyString(), anyString(), anyString());
	}

	@Test
	@DisplayName("이미 완료됐거나 취소 요청된 결제면 취소 작업을 등록하지 않는다")
	void requestCancel_notProcessing() throws Exception {
		// given
		given(paymentAttemptRepository.changeStatus(
			"paymentid", PaymentAttemptStatus.PROCESSING, PaymentAttemptStatus.CANCEL_REQUESTED)).willReturn(0);

		// when
		boolean requested = paymentAttemptService.requestCancel("paymentid", "IllegalStateException", "배달 불가");

		// then
		assertThat(requested).isFalse();
		verify(failedTaskService, never()).saveRefundPaymentTask(anyString(), anyString(), anyString());
	}
}