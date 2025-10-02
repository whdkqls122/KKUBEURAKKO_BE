package com.kkubeurakko.kkubeurakko_be.domain.users.entity;

import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Admins")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Admins {
	@Id
	@Column(name = "admin_id")
	private String id;

	@Column(name = "password", nullable = false)
	private String password;

	@Column(name = "role", nullable = false)
	private UserRole role;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stores_id", nullable = false)
	private Stores store;
}
