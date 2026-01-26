package com.kkubeurakko.kkubeurakko_be.domain.orders.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderType;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Status;
import com.kkubeurakko.kkubeurakko_be.domain.payments.dto.PaymentResponseDto;

import lombok.Builder;

@Builder
public record OrderDetailResponseDto(
	Long id,
	String orderNumber,
	OrderType orderType,
	Status orderStatus,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	LocalDateTime orderDate,
	int completionTime,
	OrderAddressResponseDto address,
	String storeRequest,
	String phoneNum,
	List<OrderMenuResponseDto> orderMenuList,
	PaymentResponseDto payment
) {
}
