package com.kkubeurakko.kkubeurakko_be.domain.payments.scheduler;

import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kkubeurakko.kkubeurakko_be.domain.payments.service.PaymentAttemptService;

@ExtendWith(MockitoExtension.class)
class PaymentAttemptSchedulerTest {

	@Mock
	private PaymentAttemptService paymentAttemptService;
	@InjectMocks
	private PaymentAttemptScheduler scheduler;

	@Test
	@DisplayName("오래 처리 중으로 남은 결제마다 취소를 요청하고, 하나가 실패해도 다음 결제를 처리한다")
	void cancelStaleAttempts() {
		// given
		given(paymentAttemptService.findProcessingPaymentIdsStartedBefore(any(LocalDateTime.class)))
			.willReturn(List.of("payment-1", "payment-2"));
		willThrow(new RuntimeException("DB 연결 실패"))
			.given(paymentAttemptService).requestCancel(eq("payment-1"), anyString(), anyString());

		// when
		scheduler.cancelStaleAttempts();

		// then
		verify(paymentAttemptService).requestCancel(eq("payment-2"), eq("STALE_PROCESSING"), anyString());
	}

	@Test
	@DisplayName("15분 전보다 먼저 시작한 처리 기록만 조회한다")
	void cancelStaleAttempts_threshold() {
		// given
		LocalDateTime before = LocalDateTime.now().minusMinutes(15);
		given(paymentAttemptService.findProcessingPaymentIdsStartedBefore(any(LocalDateTime.class)))
			.willReturn(List.of());

		// when
		scheduler.cancelStaleAttempts();

		// then
		verify(paymentAttemptService).findProcessingPaymentIdsStartedBefore(
			argThat(time -> !time.isBefore(before) && time.isBefore(before.plusSeconds(5))));
		verify(paymentAttemptService, never()).requestCancel(anyString(), anyString(), anyString());
	}
}