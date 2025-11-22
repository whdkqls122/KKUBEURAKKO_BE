package com.kkubeurakko.kkubeurakko_be.domain.orders.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderType;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Status;
import com.kkubeurakko.kkubeurakko_be.domain.payments.dto.PaymentResponseDto;

public record OrderDetailResponseDto(
	Long id,
	OrderType orderType,
	Status orderStatus,
	@JsonFormat(pattern = "yy-MM-dd HH:mm")
	LocalDateTime orderDate,
	int completionTime,
	String address,
	String phoneNum,
	List<OrderMenuResponseDto> orderMenuResponseDtoList,
	PaymentResponseDto paymentResponseDto
) {
}
