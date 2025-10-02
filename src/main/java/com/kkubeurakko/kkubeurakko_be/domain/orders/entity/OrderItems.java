package com.kkubeurakko.kkubeurakko_be.domain.orders.entity;

import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.Menus;

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
@Table(name = "OrderItems")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class OrderItems {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "order_items_id")
	private Long id;

	@Column(name = "total_price", nullable = false)
	private int totalPrice;

	@Column(name = "quantity", nullable = false)
	private int quantity;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private Orders orders;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "menus_id", nullable = false)
	private Menus menus;
}
