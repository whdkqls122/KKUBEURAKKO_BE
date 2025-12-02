package com.kkubeurakko.kkubeurakko_be.domain.orders.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderAddressSnapshot;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Orders;

public record OrderSocketResponse(
	Long orderId,
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

	public static OrderSocketResponse from(Orders order) {
		OrderAddressSnapshot snap = order.getAddressSnapshot();

		String address = null;
		if (snap != null) {
			if (snap.getRoadName() != null && snap.getDetailAddress() != null) {
				address = snap.getRoadName() + " " + snap.getDetailAddress();
			} else if (snap.getRoadName() != null) {
				address = snap.getRoadName();
			} else if (snap.getDetailAddress() != null) {
				address = snap.getDetailAddress();
			}
		}

		return new OrderSocketResponse(
			order.getId(),
			order.getOrderDate(),
			order.getPayments() != null ? order.getPayments().getPaymentsType().name() : null,
			order.getStatus().name(),
			order.getOrderType().name(),
			address,
			order.getUser().getPhone(),
			order.getStoreRequest(),
			snap != null ? snap.getRequestContent() : null,
			order.getPayments() != null ? order.getPayments().getFinalAmount() : 0,
			order.getOrderItemList()
				.stream()
				.map(OrderMenuResponse::from)
				.toList()
		);
	}

}