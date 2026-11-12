package com.kkubeurakko.kkubeurakko_be.domain.orders.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.UsersCoupons;
import com.kkubeurakko.kkubeurakko_be.domain.orders.config.OrderSocketSender;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderAddressResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderDetailResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderItemsOptionResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderMenuResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderSocketResponse;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderItems;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderType;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Orders;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Status;
import com.kkubeurakko.kkubeurakko_be.domain.orders.repository.OrderRepository;
import com.kkubeurakko.kkubeurakko_be.domain.payments.dto.PaymentResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.BusinessDay;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.StoreBusinessHours;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.StoreBusinessHoursRepository;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.service.UserService;
import com.kkubeurakko.kkubeurakko_be.domain.users.util.PhoneMaskingUtil;
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
	private final OrderSocketSender orderSocketSender;
	private final OrderDecryptService orderDecryptService;
	private final StoreBusinessHoursRepository storeBusinessHoursRepository;

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
					.orderNumber(ol.getOrderNumber())
					.name(mainMenuName)
					.orderDate(ol.getOrderDate())
					.orderQuantity(ol.getOrderItemList().stream().mapToInt(OrderItems::getQuantity).sum())
					.orderType(ol.getOrderType())
					.status(ol.getStatus())
					.completionTime(ol.getCompletionTime())
					.build();
			})
			.toList();
	}

	@Override
	@Transactional
	public void acceptOrder(Long orderId, int completionTime) {
		Orders order = orderRepository.findById(orderId)
			.orElseThrow(() -> new IllegalArgumentException("주문 없음"));

		order.accept(completionTime);

		Long storeId = extractStoreId(order);
		OrderSocketResponse response =
			orderDecryptService.toResponse(order);
		orderSocketSender.sendStatus(storeId, response);
	}

	@Override
	@Transactional
	public void completeOrder(Long orderId) {
		Orders order = orderRepository.findById(orderId)
			.orElseThrow(() -> new IllegalArgumentException("주문 없음"));

		order.complete();

		Long storeId = extractStoreId(order);
		OrderSocketResponse response =
			orderDecryptService.toResponse(order);
		orderSocketSender.sendStatus(storeId, response);
	}

	@Override
	@Transactional
	public void startDelivery(Long orderId) {

		Orders order = orderRepository.findById(orderId)
			.orElseThrow(() -> new IllegalArgumentException("주문 없음"));

		order.startDelivery();

		Long storeId = extractStoreId(order);
		OrderSocketResponse response =
			orderDecryptService.toResponse(order);

		orderSocketSender.sendStatus(storeId, response);
	}

	@Transactional
	@Override
	public void cancelOrder(Long orderId) {

		Orders order = orderRepository.findById(orderId)
			.orElseThrow(() -> new IllegalArgumentException("주문 없음"));

		order.cancel();

		UsersCoupons usersCoupon = order.getUsersCoupon();
		if (usersCoupon != null) {
			usersCoupon.rollbackUse();
		}

		Long storeId = extractStoreId(order);
		OrderSocketResponse response =
			orderDecryptService.toResponse(order);
		orderSocketSender.sendStatus(storeId, response);
	}

	@Transactional
	@Override
	public void refundOrder(Long orderId) {

		Orders order = orderRepository.findById(orderId)
			.orElseThrow(() -> new IllegalArgumentException("주문 없음"));

		order.refund();

		UsersCoupons usersCoupon = order.getUsersCoupon();
		if (usersCoupon != null) {
			usersCoupon.rollbackUse();
		}
		Long storeId = extractStoreId(order);
		OrderSocketResponse response =
			orderDecryptService.toResponse(order);
		orderSocketSender.sendStatus(storeId, response);
	}

	@Override
	@Transactional(readOnly = true)
	public List<OrderSocketResponse> getOrdersByStoreAndStatus(Long storeId, Status status) {

		BusinessDay todayDay = BusinessDay.from(LocalDate.now().getDayOfWeek());

		StoreBusinessHours hours = storeBusinessHoursRepository
			.findByStoreIdAndDayOfWeek(storeId, todayDay)
			.orElseThrow(() -> new IllegalArgumentException("영업시간 없음"));

		LocalDate today = LocalDate.now();
		LocalTime open = hours.getOpenTime();
		LocalTime close = hours.getCloseTime();

		LocalDateTime startDateTime;
		LocalDateTime endDateTime;

		if (close.isAfter(open)) {
			startDateTime = LocalDateTime.of(today, open);
			endDateTime = LocalDateTime.of(today, close);
		} else {
			startDateTime = LocalDateTime.of(today, open);
			endDateTime = LocalDateTime.of(today.plusDays(1), close);
		}

		List<Orders> orders = orderRepository
			.findByStoreIdAndStatusAndBusinessTime(
				storeId, status, startDateTime, endDateTime
			);
		return orders.stream()
			.map(order -> OrderSocketResponse.from(
				order,
				orderDecryptService.decryptAddress(order.getAddressSnapshot()),
				orderDecryptService.decryptPhone(order.getUser().getPhone())
			))
			.toList();
	}

	@Override
	public boolean isInProgressOrder(Users user) {
		return orderRepository.existsByUser_UserNumberAndStatusIn(
			user.getUserNumber(),
			List.of(
				Status.PENDING,
				Status.ACCEPTED,
				Status.ON_DELIVERY,
				Status.COOKING
			)
		);
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

		String address = null;
		String requestContent = null;

		if (order.getOrderType() == OrderType.DELIVERY && order.getAddressSnapshot() != null) {
			address =
				order.getAddressSnapshot().getRoadName()
					+ " "
					+ aesUtil.decryptFromBase64(order.getAddressSnapshot().getDetailAddress());

			requestContent = order.getAddressSnapshot().getRequestContent();
		}

		String maskingPhone = PhoneMaskingUtil.mask(aesUtil.decryptFromBase64(order.getUser().getPhone()));

		return OrderDetailResponseDto.builder()
			.id(order.getId())
			.orderNumber(order.getOrderNumber())
			.orderType(order.getOrderType())
			.orderStatus(order.getStatus())
			.orderDate(order.getOrderDate())
			.completionTime(order.getCompletionTime())
			.address(address == null
				? null
				: OrderAddressResponseDto.builder()
				.address(address)
				.requestContent(requestContent)
				.build())
			.storeRequest(order.getStoreRequest())
			.phoneNum(maskingPhone)
			.orderMenuList(orderMenuList)
			.payment(PaymentResponseDto.from(order.getPayments()))
			.build();
	}

	private Long extractStoreId(Orders order) {
		return order.getOrderItemList().stream()
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("주문 메뉴가 없습니다."))
			.getMenu()
			.getMenuGroup()
			.getStore()
			.getId();
	}
}