package com.kkubeurakko.kkubeurakko_be.domain.stores.entity;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sales")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class Sales {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "sales_id")
	private Long id;

	@DateTimeFormat(pattern = "yy-MM-dd")
	@Column(name = "date", nullable = false)
	private LocalDate date;

	@Column(name = "total_price", nullable = false)
	private Long totalPrice;

	@Column(name = "refund_price", nullable = false)
	private Long refundPrice;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stores_id", nullable = false)
	private Stores store;
}
