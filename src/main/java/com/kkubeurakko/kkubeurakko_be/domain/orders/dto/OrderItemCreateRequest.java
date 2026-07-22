package com.kkubeurakko.kkubeurakko_be.domain.orders.dto;

import java.util.List;

public record OrderItemCreateRequest(
	Long menuId,
	int quantity,
	List<Long> optionIds
) {
}