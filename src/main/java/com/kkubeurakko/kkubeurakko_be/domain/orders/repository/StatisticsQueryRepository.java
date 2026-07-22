package com.kkubeurakko.kkubeurakko_be.domain.orders.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Status;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StatisticsQueryRepository {

	private final EntityManager em;

	// 특정 날짜 매출 조회
	public Long getSalesAmount(LocalDateTime start, LocalDateTime end) {
		return em.createQuery(
				"SELECT COALESCE(SUM(o.totalAmount), 0L) " +
					"FROM Orders o " +
					"WHERE o.status = :status " +
					"AND o.orderDate BETWEEN :start AND :end",
				Long.class
			)
			.setParameter("status", Status.COMPLETED)
			.setParameter("start", start)
			.setParameter("end", end)
			.getSingleResult();
	}

	// 특정 날짜 주문 수
	public Long getOrderCount(LocalDateTime start, LocalDateTime end) {
		return em.createQuery(
				"SELECT COUNT(o.id) " +
					"FROM Orders o " +
					"WHERE o.status IN (:statuses) " +
					"AND o.orderDate BETWEEN :start AND :end",
				Long.class
			)
			.setParameter("statuses", List.of(Status.COMPLETED, Status.REFUNDED))
			.setParameter("start", start)
			.setParameter("end", end)
			.getSingleResult();
	}

	// 환불 금액 (취소된 주문)
	public Long getRefundAmount(LocalDateTime start, LocalDateTime end) {
		return em.createQuery(
				"SELECT COALESCE(SUM(p.finalAmount), 0L) " +
					"FROM Payments p " +
					"JOIN p.order o " +
					"WHERE o.status = :status " +
					"AND o.orderDate BETWEEN :start AND :end",
				Long.class
			)
			.setParameter("status", Status.REFUNDED)
			.setParameter("start", start)
			.setParameter("end", end)
			.getSingleResult();
	}

	// 할인 금액 (환불 주문 제외)
	public Long getDiscountAmount(LocalDateTime start, LocalDateTime end) {
		return em.createQuery(
				"SELECT COALESCE(SUM(p.discountAmount), 0L) " +
					"FROM Payments p " +
					"JOIN p.order o " +
					"WHERE o.status = :status " +
					"AND o.orderDate BETWEEN :start AND :end",
				Long.class
			)
			.setParameter("status", Status.COMPLETED)
			.setParameter("start", start)
			.setParameter("end", end)
			.getSingleResult();
	}

}
