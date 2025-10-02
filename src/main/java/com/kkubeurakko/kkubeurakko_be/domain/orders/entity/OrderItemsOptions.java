package com.kkubeurakko.kkubeurakko_be.domain.orders.entity;

import com.kkubeurakko.kkubeurakko_be.domain.options.entity.Options;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "OrderItemsOptions")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class OrderItemsOptions {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "order_items_options_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_items_id", nullable = false)
	private OrderItems orderItems;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "options_id", nullable = false)
	private Options option;
}
