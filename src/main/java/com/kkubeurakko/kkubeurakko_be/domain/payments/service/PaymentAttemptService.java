package com.kkubeurakko.kkubeurakko_be.domain.payments.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.kkubeurakko.kkubeurakko_be.domain.payments.entity.PaymentAttemptStatus;

public interface PaymentAttemptService {
	//결제 처리 시작 기록 (같은 결제가 이미 기록돼 있으면 거절)
	void start(String paymentId, boolean cashReceipt);

	//결제가 완료되지 않았거나 우리 상점 결제가 아니면 기록 삭제
	void discard(String paymentId);

	//주문 트랜잭션 안에서 처리 완료
	void complete(String paymentId);

	//처리 중인 결제를 취소 요청 상태로 바꾸고 취소 작업 등록, 바꿨으면 true
	boolean requestCancel(String paymentId, String reasonType, String reasonMessage);

	Optional<PaymentAttemptStatus> findStatus(String paymentId);

	//주어진 시각 전에 시작해 아직 처리 중인 결제
	List<String> findProcessingPaymentIdsStartedBefore(LocalDateTime time);
}