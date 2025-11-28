package com.kkubeurakko.kkubeurakko_be.domain.orders.dto;

import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderItems;

public record OrderMenuResponse(
	String menuName,
	int price,
	int quantity,
	List<String> options
) {
	public static OrderMenuResponse from(OrderItems item) {
		return new OrderMenuResponse(
			item.getMenu().getName(),
			item.getMenu().getPrice(),
			item.getQuantity(),
			item.getOrderItemOptionList().stream()
				.map(o -> o.getOption().getName())
				.toList()
		);
	}
}