package com.kkubeurakko.kkubeurakko_be.domain.payments.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kkubeurakko.kkubeurakko_be.domain.payments.entity.PaymentAttempt;
import com.kkubeurakko.kkubeurakko_be.domain.payments.entity.PaymentAttemptStatus;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, Long> {

	Optional<PaymentAttempt> findByPaymentId(String paymentId);

	List<PaymentAttempt> findByStatusAndCreateAtBefore(PaymentAttemptStatus status, LocalDateTime createAt);

	void deleteByPaymentIdAndStatus(String paymentId, PaymentAttemptStatus status);

	// 현재 상태가 from일 때만 to로 바꾼다. 바뀐 행 수(0 또는 1)를 반환한다.
	@Modifying
	@Query("update PaymentAttempt a set a.status = :to where a.paymentId = :paymentId and a.status = :from")
	int changeStatus(
		@Param("paymentId") String paymentId,
		@Param("from") PaymentAttemptStatus from,
		@Param("to") PaymentAttemptStatus to
	);
}