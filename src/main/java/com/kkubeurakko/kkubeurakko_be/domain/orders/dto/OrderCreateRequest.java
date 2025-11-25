package com.kkubeurakko.kkubeurakko_be.domain.orders.dto;

import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderType;

public record OrderCreateRequest(
	OrderType orderType,
	String address,
	String storeRequest,
	List<OrderItemCreateRequest> items
) {
}