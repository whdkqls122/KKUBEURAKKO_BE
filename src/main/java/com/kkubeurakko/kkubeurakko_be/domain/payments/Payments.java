package com.kkubeurakko.kkubeurakko_be.domain.payments;

import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Orders;
import com.kkubeurakko.kkubeurakko_be.global.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Payments")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Payments extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "payments_id")
	private Long id;

	@Column(name = "total_amount", nullable = false)
	private int totalAmount;

	@Column(name = "transaction_id", nullable = false, length = 50)
	private String transactionId;

	@Column(name = "payments_type", nullable = false, length = 50)
	private String paymentsType;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "orders_id")
	private Orders order;


}
