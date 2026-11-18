package com.kkubeurakko.kkubeurakko_be.domain.payments.scheduler;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kkubeurakko.kkubeurakko_be.domain.payments.service.PaymentAttemptService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 주문이 완료되지 않은 채 오래 처리 중으로 남은 결제를 찾아 취소를 요청
 * 커밋 시점 실패, 서버 종료, 취소 작업 등록 실패, 결제 조회 실패처럼 롤백 이벤트로 처리되지 못한 경우를 모두 여기서 처리
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentAttemptScheduler {
	// 정상적인 주문 처리 시간(PortOne 타임아웃 포함)보다 충분히 길게 잡음
	static final Duration STALE_AFTER = Duration.ofMinutes(15);

	private final PaymentAttemptService paymentAttemptService;
	private final Clock clock;

	@Scheduled(fixedDelay = 60000)
	public void cancelStaleAttempts() {
		List<String> paymentIds =
			paymentAttemptService.findProcessingPaymentIdsStartedBefore(LocalDateTime.now(clock).minus(STALE_AFTER));

		for (String paymentId : paymentIds) {
			try {
				if (paymentAttemptService.requestCancel(
					paymentId, "STALE_PROCESSING", "주문이 완료되지 않은 채 처리 중으로 남은 결제")) {
					log.warn("오래 처리 중으로 남은 결제 취소 요청 (paymentId={})", paymentId);
				}
			} catch (Exception e) {
				// 다음 주기에 다시 시도한다
				log.error("오래 처리 중으로 남은 결제 취소 요청 실패 (paymentId={})", paymentId, e);
			}
		}
	}
}