package com.kkubeurakko.kkubeurakko_be.domain.orders.service;

import org.springframework.stereotype.Component;

import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderSocketResponse;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderAddressSnapshot;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Orders;
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
			try {
				detail = aesUtil.decryptFromBase64(detail);
			} catch (Exception e) {
			}
		}

		return (road != null ? road : "") +
			(detail != null ? " " + detail : "");
	}

	public OrderSocketResponse toResponse(Orders order) {

		String address = decryptAddress(order.getAddressSnapshot());

		String phone =
			order.getAddressSnapshot() != null
				? decryptPhone(order.getUser().getPhone())
				: null;

		return OrderSocketResponse.from(order, address, phone);
	}
}