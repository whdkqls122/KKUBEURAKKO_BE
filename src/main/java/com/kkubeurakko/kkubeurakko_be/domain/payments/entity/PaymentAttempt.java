package com.kkubeurakko.kkubeurakko_be.domain.payments.entity;

import com.kkubeurakko.kkubeurakko_be.global.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제 처리 기록. 주문 처리 전에 먼저 커밋해 두고 주문 커밋과 같은 트랜잭션에서 완료로 바꾼다.
 * 완료되지 않은 기록은 어떤 이유로 실패했든 결제 취소 대상으로 남는다.
 */
@Entity
@Table(
	name = "payment_attempt",
	indexes = @Index(name = "idx_payment_attempt_status_created_at", columnList = "status, created_at")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentAttempt extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "payment_attempt_id")
	private Long id;

	@Column(name = "payment_id", nullable = false, unique = true, length = 100)
	private String paymentId;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 30)
	private PaymentAttemptStatus status;

	@Column(name = "cash_receipt", nullable = false)
	private boolean cashReceipt;

	private PaymentAttempt(String paymentId, boolean cashReceipt) {
		this.paymentId = paymentId;
		this.cashReceipt = cashReceipt;
		this.status = PaymentAttemptStatus.PROCESSING;
	}

	public static PaymentAttempt start(String paymentId, boolean cashReceipt) {
		return new PaymentAttempt(paymentId, cashReceipt);
	}
}