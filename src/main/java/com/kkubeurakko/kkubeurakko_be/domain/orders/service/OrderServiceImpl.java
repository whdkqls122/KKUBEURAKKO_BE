package com.kkubeurakko.kkubeurakko_be.domain.orders.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderItems;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Orders;
import com.kkubeurakko.kkubeurakko_be.domain.orders.repository.OrderItemOptionRepository;
import com.kkubeurakko.kkubeurakko_be.domain.orders.repository.OrderItemRepository;
import com.kkubeurakko.kkubeurakko_be.domain.orders.repository.OrderRepository;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.service.UserService;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final OrderItemOptionRepository orderItemOptionRepository;
	private final UserService userService;

	@Override
	@Transactional(readOnly = true)
	public List<OrderResponseDto> getAllOrderHistory(CustomOAuth2User customOAuth2User) {
		Users user = userService.getUser(customOAuth2User.getName());
		List<Orders> orderList = orderRepository.findByUser(user);

		return toResponseDto(orderList);
	}

	private List<OrderResponseDto> toResponseDto(List<Orders> orderList) {
		if (orderList == null || orderList.isEmpty()) {
			return List.of();
		}
		return orderList.stream()
			.map(ol -> {
				String mainMenuName = ol.getOrderItemList().stream()
					.map(oi -> oi.getMenus().getName())
					.findFirst().orElseThrow(() -> new EntityNotFoundException("조회된 메뉴 이름이 없습니다."));

				return OrderResponseDto.builder()
					.id(ol.getId())
					.name(mainMenuName)
					.orderDate(ol.getOrderDate())
					.orderQuantity(ol.getOrderItemList().stream().mapToInt(OrderItems::getQuantity).sum())
					.status(ol.getStatus())
					.build();
			})
			.toList();
	}
}
