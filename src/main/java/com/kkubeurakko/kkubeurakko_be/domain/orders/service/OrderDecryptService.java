package com.kkubeurakko.kkubeurakko_be.domain.orders.service;

import org.springframework.stereotype.Component;

import com.kkubeurakko.kkubeurakko_be.domain.addresses.entity.Addresses;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderAddressSnapshot;
import com.kkubeurakko.kkubeurakko_be.global.security.util.AesUtil;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderDecryptService {

	private final AesUtil aesUtil;

	public String decryptPhone(String encryptedPhone) {
		if (encryptedPhone == null)
			return null;
		return aesUtil.decryptFromBase64(encryptedPhone);
	}

	public OrderAddressSnapshot createSnapshot(Addresses address) {
		if (address == null)
			return null;

		return OrderAddressSnapshot.builder()
			.addressName(address.getAddressName())
			.roadName(address.getRoadName())
			.detailAddress(aesUtil.decryptFromBase64(address.getDetailAddress()))
			.requestContent(address.getRequestContent())
			.entrancePassword(address.getEntrancePassword())
			.directionsInfo(address.getDirectionsInfo())
			.build();
	}
}