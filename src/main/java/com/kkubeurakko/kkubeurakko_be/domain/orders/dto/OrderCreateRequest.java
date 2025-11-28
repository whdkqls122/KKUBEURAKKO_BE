package com.kkubeurakko.kkubeurakko_be.domain.orders.dto;

import java.time.LocalDateTime;

import com.kkubeurakko.kkubeurakko_be.domain.addresses.entity.Addresses;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderType;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Orders;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Status;
import com.kkubeurakko.kkubeurakko_be.domain.payments.entity.PaymentType;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;

import io.micrometer.common.lang.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record OrderCreateRequest(
	@NotNull(message = "사용자 ID는 필수입니다.")
	Long userId,

	@Nullable
	Long addressId,

	@Nullable
	@Size(max = 15, message = "가게 요청사항은 최대 15자까지 가능합니다.")
	String storeRequest,

	@Nullable
	@Size(max = 15, message = "배달 요청사항은 최대 15자까지 가능합니다.")
	String deliveryRequest,

	@NotNull(message = "주문 타입은 필수입니다.")
	OrderType orderType,

	@NotNull(message = "결제 방식은 필수입니다.")
	PaymentType paymentType,

	@Nullable
	Long couponId,

	@PositiveOrZero(message = "배달팁은 0 이상이어야 합니다.")
	int deliveryTip,

	@Positive(message = "총 금액은 0보다 커야 합니다.")
	int totalAmount,

	@PositiveOrZero(message = "할인 금액은 0 이상이어야 합니다.")
	int discountAmount
) {
	public Orders toOrderEntity(Users user, Addresses address, int totalAmount) {
		return Orders.builder()
			.user(user)
			.orderDate(LocalDateTime.now())
			.status(Status.PENDING)
			.orderType(orderType)
			.address(address != null ? address.getRoadName() + " " + address.getDetailAddress() : null)
			.storeRequest(storeRequest)
			.completionTime(0)
			.totalAmount(totalAmount)
			.build();
	}
}
