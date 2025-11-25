package com.kkubeurakko.kkubeurakko_be.domain.orders.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderDetailResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderItemsOptionResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderMenuResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderItems;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Orders;
import com.kkubeurakko.kkubeurakko_be.domain.orders.repository.OrderRepository;
import com.kkubeurakko.kkubeurakko_be.domain.payments.dto.PaymentResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.service.UserService;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;
import com.kkubeurakko.kkubeurakko_be.global.security.util.AesUtil;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
	private final OrderRepository orderRepository;
	private final UserService userService;
	private final AesUtil aesUtil;

	@Override
	@Transactional(readOnly = true)
	public List<OrderResponseDto> getAllOrderHistory(CustomOAuth2User customOAuth2User) {
		Users user = userService.getUser(customOAuth2User.getName());
		List<Orders> orderList = orderRepository.findByUser(user);

		return toResponseDto(orderList);
	}

	@Override
	@Transactional(readOnly = true)
	public OrderDetailResponseDto getOrderDetailHistory(Long orderId, CustomOAuth2User customOAuth2User) {
		Users user = userService.getUser(customOAuth2User.getName());

		Orders order = orderRepository.findByIdAndUser(orderId, user)
			.orElseThrow(() -> new IllegalArgumentException("조회된 주문 내역이 없습니다."));

		return toResponseDto(order);
	}

	private List<OrderResponseDto> toResponseDto(List<Orders> orderList) {
		if (orderList == null || orderList.isEmpty()) {
			return List.of();
		}
		return orderList.stream()
			.map(ol -> {
				String mainMenuName = ol.getOrderItemList().stream()
					.map(oi -> oi.getMenu().getName())
					.findFirst().orElseThrow(() -> new EntityNotFoundException("조회된 메뉴 이름이 없습니다."));

				return OrderResponseDto.builder()
					.id(ol.getId())
					.name(mainMenuName)
					.orderDate(ol.getOrderDate())
					.orderQuantity(ol.getOrderItemList().stream().mapToInt(OrderItems::getQuantity).sum())
					.orderType(ol.getOrderType())
					.status(ol.getStatus())
					.build();
			})
			.toList();
	}

	private OrderDetailResponseDto toResponseDto(Orders order) {

		List<OrderMenuResponseDto> orderMenuList = order.getOrderItemList().stream()
			.map(oi -> OrderMenuResponseDto.builder()
				.name(oi.getMenu().getName())
				.quantity(oi.getQuantity())
				.totalPrice(oi.getTotalPrice())
				.optionList(
					oi.getOrderItemOptionList().stream()
						.map(opt -> OrderItemsOptionResponseDto.builder()
							.name(opt.getOption().getName())
							.price(opt.getOption().getPrice())
							.build()
						)
						.toList()
				)
				.build()
			)
			.toList();

		return OrderDetailResponseDto.builder()
			.id(order.getId())
			.orderType(order.getOrderType())
			.orderStatus(order.getStatus())
			.orderDate(order.getOrderDate())
			.completionTime(order.getCompletionTime())
			.address(order.getAddress())
			.phoneNum(aesUtil.decryptFromBase64(order.getUser().getPhone()))
			.orderMenuList(orderMenuList)
			.paymentResponseDto(PaymentResponseDto.from(order.getPayments()))
			.build();
	}
}
