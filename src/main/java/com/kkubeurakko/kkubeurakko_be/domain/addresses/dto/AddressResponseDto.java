package com.kkubeurakko.kkubeurakko_be.domain.addresses.dto;

import com.kkubeurakko.kkubeurakko_be.domain.addresses.entity.AddressCategory;

import lombok.Builder;

@Builder
public record AddressResponseDto(
	Long id,
	String address,
	boolean isPrimary,
	AddressCategory category,
	String requestContent,
	String entrancePassword,
	String directionsInfo
) {
}
