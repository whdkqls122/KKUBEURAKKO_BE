package com.kkubeurakko.kkubeurakko_be.domain.orders.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderAddressSnapshot;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Orders;

import lombok.Builder;

@Builder
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

		return OrderSocketResponse.builder()
			.orderId(order.getId())
			.orderDate(order.getOrderDate())
			.paymentType(
				order.getPayments() != null
					? order.getPayments().getPaymentsType().name()
					: null
			)
			.status(order.getStatus().name())
			.orderType(order.getOrderType().name())
			.address(address)
			.phone(order.getUser().getPhone())
			.storeRequest(order.getStoreRequest())
			.deliveryRequest(snap != null ? snap.getRequestContent() : null)
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