package com.kkubeurakko.kkubeurakko_be.domain.stores.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.Coupons;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuGroups;
import com.kkubeurakko.kkubeurakko_be.global.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Stores")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class Stores extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "stores_id")
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private StoreStatus status;

	@Column(name = "min_order_price", nullable = false)
	private int minOrderPrice;

	@Column(name = "packaging_avg_time", nullable = false)
	private int packagingAvgTime;

	@Column(name = "delivery_avg_time", nullable = false)
	private int deliveryAvgTime;

	@Column(name = "pause_start_at")
	private LocalDateTime pauseStartAt;

	@Column(name = "pause_end_at")
	private LocalDateTime pauseEndAt;

	@Column(name = "latitude", nullable = false)
	private double latitude;

	@Column(name = "longitude", nullable = false)
	private double longitude;

	@OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Coupons> couponsList = new ArrayList<>();

	@OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<MenuGroups> menuGroupsList = new ArrayList<>();

	@OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Sales> salesList = new ArrayList<>();

	@OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<StoreBusinessHours> storeBusinessHoursList = new ArrayList<>();

	@OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<DeliveryRegion> deliveryRegionList = new ArrayList<>();

	@OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<StoreHoliday> storeHolidayList = new ArrayList<>();

	public void updateBasicInfo(
		Integer minOrderPrice,
		Integer deliveryAvgTime,
		Integer packagingAvgTime
	) {
		if (minOrderPrice != null) {
			this.minOrderPrice = minOrderPrice;
		}
		if (deliveryAvgTime != null) {
			this.deliveryAvgTime = deliveryAvgTime;
		}
		if (packagingAvgTime != null) {
			this.packagingAvgTime = packagingAvgTime;
		}
	}

	public void open() {
		this.status = StoreStatus.OPEN;
	}

	public void close() {
		this.status = StoreStatus.CLOSED;
	}

	public void pause(LocalDateTime start, LocalDateTime end) {
		this.status = StoreStatus.PAUSED;
		this.pauseStartAt = start;
		this.pauseEndAt = end;
	}

	public void reopen() {
		this.status = StoreStatus.OPEN;
		this.pauseStartAt = null;
		this.pauseEndAt = null;
	}
}
