package com.kkubeurakko.kkubeurakko_be.domain.stores.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.Coupons;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.UsersCoupons;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuGroups;
import com.kkubeurakko.kkubeurakko_be.global.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Stores")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Stores extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "stores_id")
	private Long id;

	@Column(name = "address", nullable = false, length = 100)
	private String address;

	@Column(name = "tel", nullable = false, length = 13)
	private String tel;

	@Column(name = "open_time", nullable = false)
	private LocalDate openTime;

	@Column(name = "close_time", nullable = false)
	private LocalDate closeTime;

	@Column(name = "is_open", nullable = false)
	private boolean isOpen;

	@Column(name = "min_order_price", nullable = false)
	private int minOrderPrice;

	@Column(name = "delivery_region", nullable = false, length = 250)
	private String delivery_region;

	@OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Coupons> couponsList = new ArrayList<>();

	@OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<MenuGroups> menuGroupsList = new ArrayList<>();

	@OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Sales> salesList = new ArrayList<>();
}
