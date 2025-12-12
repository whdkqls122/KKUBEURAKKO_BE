package com.kkubeurakko.kkubeurakko_be.domain.orders.repository;

import java.time.LocalDateTime;

import org.springframework.stereotype.Repository;

import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Status;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@
	Repository
@RequiredArgsConstructor
public class StatisticsQueryRepository {

	private final EntityManager em;

	//특정 날짜 매출 조회
	public Integer getSalesAmount(LocalDateTime start, LocalDateTime end) {
		return em.createQuery(
				"SELECT COALESCE(SUM(o.totalAmount), 0) " +
					"FROM Orders o " +
					"WHERE o.status = :status " +
					"AND o.orderDate BETWEEN :start AND :end", Integer.class)
			.setParameter("status", Status.COMPLETED)
			.setParameter("start", start)
			.setParameter("end", end)
			.getSingleResult();
	}

	// 특정 날짜 주문 수 조회
	public Long getOrderCount(LocalDateTime start, LocalDateTime end) {
		return em.createQuery(
				"SELECT COUNT(o.id) FROM Orders o " +
					"WHERE o.status = :status " +
					"AND o.orderDate BETWEEN :start AND :end", Long.class)
			.setParameter("status", Status.COMPLETED)
			.setParameter("start", start)
			.setParameter("end", end)
			.getSingleResult();
	}

	// 환불 금액
	public Integer getRefundAmount(LocalDateTime start, LocalDateTime end) {
		return em.createQuery(
				"SELECT COALESCE(SUM(p.finalAmount), 0) " +
					"FROM Payments p " +
					"JOIN p.order o " +
					"WHERE o.status = :status " +
					"AND o.orderDate BETWEEN :start AND :end", Integer.class)
			.setParameter("status", Status.REFUNDED)
			.setParameter("start", start)
			.setParameter("end", end)
			.getSingleResult();
	}

	// 할인 금액
	public Integer getDiscountAmount(LocalDateTime start, LocalDateTime end) {
		return em.createQuery(
				"SELECT COALESCE(SUM(p.discountAmount), 0) " +
					"FROM Payments p " +
					"JOIN p.order o " +
					"WHERE o.orderDate BETWEEN :start AND :end", Integer.class)
			.setParameter("start", start)
			.setParameter("end", end)
			.getSingleResult();
	}
}