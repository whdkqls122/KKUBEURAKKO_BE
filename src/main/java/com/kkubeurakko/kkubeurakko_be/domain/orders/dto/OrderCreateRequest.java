package com.kkubeurakko.kkubeurakko_be.domain.orders.dto;

import java.time.LocalDateTime;

import com.kkubeurakko.kkubeurakko_be.domain.addresses.entity.Addresses;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderType;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Orders;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Status;
import com.kkubeurakko.kkubeurakko_be.domain.payments.entity.PaymentType;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;

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
	public Orders toOrderEntity(Users user, Addresses address, int totalAmount) {
		return Orders.builder()
			.user(user)
			.orderDate(LocalDateTime.now())
			.status(Status.PENDING)
			.orderType(orderType)
			.address(address != null ? address.getRoadName() + " " + address.getDetailAddress() : null)
			.storeRequest(storeRequest)
			.completionTime(0)
			.totalAmount(totalAmount)
			.build();
	}
}
