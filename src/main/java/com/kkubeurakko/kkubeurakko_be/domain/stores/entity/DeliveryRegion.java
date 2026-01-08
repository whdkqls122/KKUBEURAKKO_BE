package com.kkubeurakko.kkubeurakko_be.domain.stores.entity;

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
@Table(name = "DeliveryRegion")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class DeliveryRegion {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "delivery_region_id")
	private Long id;

	@Column(name = "address_name", nullable = false, length = 50)
	private String addressName;

	@Column(name = "is_available", nullable = false)
	private boolean isAvailable;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stores_id", nullable = false)
	private Stores store;
}
