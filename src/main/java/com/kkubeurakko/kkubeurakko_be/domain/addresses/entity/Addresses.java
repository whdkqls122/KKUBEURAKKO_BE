package com.kkubeurakko.kkubeurakko_be.domain.addresses.entity;

import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.global.BaseEntity;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Addresses")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Addresses extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "address_id")
	private Long id;

	@Column(name = "addres_name", nullable = false, length = 30)
	private String addressName;

	@Column(name = "road_name", nullable = false, length = 50)
	private String roadName;

	@Column(name = "detail_address", nullable = false, length = 20)
	private String detailAddress;

	@Column(name = "postal_code", nullable = false, length = 5)
	private String postalCode;

	@Column(name = "is_primary", nullable = false)
	private boolean isPrimary;

	@Enumerated(EnumType.STRING)
	@Column(name = "category", nullable = false)
	private AddressCategory category;

	@Column(name = "request_content", length = 100)
	private String requestContent;

	@Column(name = "entrance_password", length = 20)
	private String entrancePassword;

	@Column(name = "directions_info", length = 100)
	private String directionsInfo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_number", nullable = false)
	private Users users;
}
