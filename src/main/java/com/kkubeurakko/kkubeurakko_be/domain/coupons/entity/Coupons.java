package com.kkubeurakko.kkubeurakko_be.domain.coupons.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;
import com.kkubeurakko.kkubeurakko_be.global.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Coupons")
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Coupons extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "coupons_id")
	private Long id;

	@Column(name = "name", nullable = false, length = 50)
	private String name;

	@Column(name = "discount_amount", nullable = false, length = 10)
	private String discountAmount;

	@Column(name = "min_order_amount", nullable = false)
	private int minOrderAmount;

	@Enumerated(EnumType.STRING)
	@Column(name = "coupon_type", nullable = false)
	private CouponType couponType;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@Column(name = "valid_from", nullable = false)
	private LocalDate validFrom;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@Column(name = "valid_until", nullable = false)
	private LocalDate validUntil;

	@Column(name = "quantity", nullable = false)
	private int quantity;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private Status status;

	@OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<UsersCoupons> usersCouponsList = new ArrayList<>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stroes_id", nullable = false)
	private Stores store;

	public void updateQuantity() {
		if (this.quantity <= 0) {
			throw new IllegalStateException("쿠폰 수량이 부족합니다.");
		}
		this.quantity -= 1;
	}
}
