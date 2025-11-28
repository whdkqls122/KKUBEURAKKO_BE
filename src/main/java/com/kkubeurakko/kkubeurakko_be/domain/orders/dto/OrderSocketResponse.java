package com.kkubeurakko.kkubeurakko_be.domain.orders.dto;

import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Orders;

public record OrderSocketResponse(
	Long orderId,
	String orderDate,
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

	public static OrderSocketResponse from(Orders order) {
		return new OrderSocketResponse(
			order.getId(),
			order.getOrderDate().toString(),
			order.getPayments().getPaymentsType().name(),
			order.getStatus().name(),
			order.getOrderType().name(),
			order.getAddress(),
			order.getUser().getPhone(),
			order.getStoreRequest(),
			null,
			order.getPayments().getFinalAmount(),
			order.getOrderItemList().stream()
				.map(OrderMenuResponse::from)
				.toList()
		);
	}
}