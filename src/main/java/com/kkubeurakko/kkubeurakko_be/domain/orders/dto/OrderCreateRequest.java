package com.kkubeurakko.kkubeurakko_be.domain.orders.dto;

import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderType;
import com.kkubeurakko.kkubeurakko_be.domain.payments.entity.PaymentType;

public record OrderCreateRequest(
	Long userId,
	Long addressId,
	String storeRequest,
	String deliveryRequest,
	OrderType orderType, // DELIVERY, TAKEOUT
	PaymentType paymentType,
	Long couponId, // optional
	int deliveryTip,
	int totalAmount,
	int discountAmount
) {
}
