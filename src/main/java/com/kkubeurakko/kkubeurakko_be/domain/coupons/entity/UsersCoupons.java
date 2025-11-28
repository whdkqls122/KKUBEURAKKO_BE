package com.kkubeurakko.kkubeurakko_be.domain.coupons.entity;

import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;

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
@Table(name = "Users_Coupons")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class UsersCoupons {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
	@Column(name = "users_coupons_id")
	private Long id;

	@Column(name = "coupons_number", nullable = false, length = 19)
	private String couponsNumber;

	@Column(name = "is_used", nullable = false)
	private boolean isUsed;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_number", nullable = false)
	private Users user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "coupons_id", nullable = false)
	private Coupons coupon;
}
