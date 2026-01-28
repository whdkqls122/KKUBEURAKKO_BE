package com.kkubeurakko.kkubeurakko_be.domain.users.entity;

import java.util.ArrayList;
import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.addresses.entity.Addresses;
import com.kkubeurakko.kkubeurakko_be.domain.cart.entity.Cart;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.UsersCoupons;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Orders;
import com.kkubeurakko.kkubeurakko_be.global.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Users")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class Users extends BaseEntity {
	@Id
	@Column(name = "user_number")
	private String userNumber;

	@Column(name = "phone", unique = true, nullable = false)
	private String phone;

	@Column(name = "phone_hash", unique = true, nullable = false)
	private String phoneHash;

	@Column(name = "nickname", nullable = false)
	private String nickname;

	@Column(name = "email")
	private String email;

	@Column(name = "id", unique = true, updatable = false, length = 20)
	private String id;

	@Column(name = "password")
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false)
	private UserRole role;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<UsersCoupons> usersCoupons = new ArrayList<>();

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Addresses> addressesList = new ArrayList<>();

	@OneToMany(mappedBy = "user")
	private List<Orders> ordersList = new ArrayList<>();

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "cart_id")
	private Cart cart;

	public void updateNickname(String nickname) {
		this.nickname = nickname;
	}

	public void updateCart(Cart cart) {
		this.cart = cart;
	}
}
