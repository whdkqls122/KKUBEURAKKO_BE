package com.kkubeurakko.kkubeurakko_be.domain.orders.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Orders;

import lombok.Builder;

@Builder
public record OrderSocketResponse(
	String orderId,
	LocalDateTime orderDate,
	String paymentType,
	String status,
	String orderType,
	String address,
	String phone,
	String storeRequest,
	String deliveryRequest,
	int finalAmount,
	List<OrderMenuResponse> items
) {

	public static OrderSocketResponse from(
		Orders order,
		String address,
		String phone
	) {
		return OrderSocketResponse.builder()
			.orderId(order.getOrderNumber())
			.orderDate(order.getOrderDate())
			.paymentType(
				order.getPayments() != null
					? order.getPayments().getPaymentsType().name()
					: null
			)
			.status(order.getStatus().name())
			.orderType(order.getOrderType().name())
			.address(address)
			.phone(phone)
			.storeRequest(order.getStoreRequest())
			.deliveryRequest(
				order.getAddressSnapshot() != null
					? order.getAddressSnapshot().getRequestContent()
					: null
			)
			.finalAmount(
				order.getPayments() != null
					? order.getPayments().getFinalAmount()
					: 0
			)
			.items(
				order.getOrderItemList()
					.stream()
					.map(OrderMenuResponse::from)
					.toList()
			)
			.build();
	}
}
