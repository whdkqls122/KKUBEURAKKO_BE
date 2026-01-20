package com.kkubeurakko.kkubeurakko_be.domain.orders.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderType;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Status;

import lombok.Builder;

@Builder
public record OrderResponseDto(
	Long id,
	String orderNumber,
	String name,
	int orderQuantity,
	OrderType orderType,
	Status status,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	LocalDateTime orderDate
) {
}
