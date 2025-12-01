package com.kkubeurakko.kkubeurakko_be.domain.orders.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderAddressSnapshot {

	@Column(name = "address_name", length = 50)
	private String addressName;

	@Column(name = "road_name", length = 50)
	private String roadName;

	@Column(name = "detail_address")
	private String detailAddress;

	@Column(name = "request_content", length = 100)
	private String requestContent;

	@Column(name = "entrance_password")
	private String entrancePassword;

	@Column(name = "directions_info", length = 100)
	private String directionsInfo;
}