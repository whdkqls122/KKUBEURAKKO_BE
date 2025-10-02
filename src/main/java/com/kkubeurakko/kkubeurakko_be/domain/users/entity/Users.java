package com.kkubeurakko.kkubeurakko_be.domain.users.entity;

import java.util.ArrayList;
import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.addresses.entity.Addresses;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.UsersCoupons;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Orders;
import com.kkubeurakko.kkubeurakko_be.global.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Users extends BaseEntity {
	@Id
	@Column(name = "user_number")
	private String user_number;

	@Column(name = "phone", length = 13, unique = true, nullable = false)
	private String phone;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false)
	private UserRole role;

	@Column(name = "nickname", nullable = false)
	private String nickname;

	@OneToMany(mappedBy = "Users", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<UsersCoupons> usersCoupons = new ArrayList<>();

	@OneToMany(mappedBy = "Addresses", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Addresses> addressesList = new ArrayList<>();

	@OneToMany(mappedBy = "Orders", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Orders> ordersList = new ArrayList<>();
}
