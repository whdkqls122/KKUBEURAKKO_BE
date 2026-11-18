package com.kkubeurakko.kkubeurakko_be.domain.payments.scheduler;

import static org.mockito.BDDMockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kkubeurakko.kkubeurakko_be.domain.payments.service.PaymentAttemptService;

@ExtendWith(MockitoExtension.class)
class PaymentAttemptSchedulerTest {

	// 2026-11-18 12:00 (Asia/Seoul)
	private static final Clock FIXED_CLOCK =
		Clock.fixed(Instant.parse("2026-11-18T03:00:00Z"), ZoneId.of("Asia/Seoul"));

	@Mock
	private PaymentAttemptService paymentAttemptService;

	private PaymentAttemptScheduler scheduler;

	@BeforeEach
	void setUp() {
		scheduler = new PaymentAttemptScheduler(paymentAttemptService, FIXED_CLOCK);
	}

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
	@DisplayName("현재 시각보다 정확히 15분 전에 시작한 처리 기록까지 조회한다")
	void cancelStaleAttempts_threshold() {
		// given
		given(paymentAttemptService.findProcessingPaymentIdsStartedBefore(any(LocalDateTime.class)))
			.willReturn(List.of());

		// when
		scheduler.cancelStaleAttempts();

		// then
		verify(paymentAttemptService).findProcessingPaymentIdsStartedBefore(LocalDateTime.of(2026, 11, 18, 11, 45));
		verify(paymentAttemptService, never()).requestCancel(anyString(), anyString(), anyString());
	}
}