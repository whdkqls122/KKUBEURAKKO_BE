package com.kkubeurakko.kkubeurakko_be.domain.orders.dto;

import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Orders;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderStatusSocketEvent {

	private Long orderId;
	private String status;
	private Integer completionTime;

	public static OrderStatusSocketEvent from(Orders order) {
		return OrderStatusSocketEvent.builder()
			.orderId(order.getId())
			.status(order.getStatus().name())
			.completionTime(order.getCompletionTime())
			.build();
	}
}
