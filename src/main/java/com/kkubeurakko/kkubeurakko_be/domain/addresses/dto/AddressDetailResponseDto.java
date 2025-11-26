package com.kkubeurakko.kkubeurakko_be.domain.addresses.dto;

import com.kkubeurakko.kkubeurakko_be.domain.addresses.entity.AddressCategory;

import lombok.Builder;

@Builder
public record AddressDetailResponseDto(
	Long id,
	String addressName,
	String roadName,
	String detailAddress,
	String postalCode,
	double latitude,
	double longitude,
	boolean isPrimary,
	AddressCategory category,
	String requestContent,
	String entrancePassword,
	String directionsInfo
) {
}
