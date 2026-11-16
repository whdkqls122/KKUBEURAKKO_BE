package com.kkubeurakko.kkubeurakko_be.domain.payments.entity;

public enum PaymentAttemptStatus {
	PROCESSING,       // 결제 확인 후 주문 처리 중
	COMPLETED,        // 주문 커밋과 함께 완료
	CANCEL_REQUESTED  // 주문이 완료되지 않아 결제 취소 작업 등록
}