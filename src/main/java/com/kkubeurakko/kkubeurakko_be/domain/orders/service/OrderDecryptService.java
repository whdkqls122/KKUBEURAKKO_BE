package com.kkubeurakko.kkubeurakko_be.domain.orders.service;

import org.springframework.stereotype.Component;

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

	public String decryptAddress(OrderAddressSnapshot snap) {
		if (snap == null)
			return null;

		String road = snap.getRoadName();
		String detail = snap.getDetailAddress();

		if (detail != null) {
			detail = aesUtil.decryptFromBase64(detail);
		}

		return (road != null ? road : "") +
			(detail != null ? " " + detail : "");
	}
}