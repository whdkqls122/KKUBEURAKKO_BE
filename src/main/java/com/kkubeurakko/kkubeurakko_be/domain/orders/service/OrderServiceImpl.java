package com.kkubeurakko.kkubeurakko_be.domain.orders.service;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kkubeurakko.kkubeurakko_be.domain.addresses.entity.Addresses;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.repository.AddressRepository;
import com.kkubeurakko.kkubeurakko_be.domain.cart.entity.Cart;
import com.kkubeurakko.kkubeurakko_be.domain.cart.entity.CartItem;
import com.kkubeurakko.kkubeurakko_be.domain.cart.entity.CartItemOption;
import com.kkubeurakko.kkubeurakko_be.domain.cart.repository.CartRepository;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.Coupons;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.UsersCoupons;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.repository.UserCouponRepository;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderAddressResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderCreateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderDetailResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderItemsOptionResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderMenuResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderSocketResponse;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderItems;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderItemsOptions;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderType;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Orders;
import com.kkubeurakko.kkubeurakko_be.domain.orders.exception.OrderCreateException;
import com.kkubeurakko.kkubeurakko_be.domain.orders.repository.OrderRepository;
import com.kkubeurakko.kkubeurakko_be.domain.orders.util.OrderNumberUtil;
import com.kkubeurakko.kkubeurakko_be.domain.payments.dto.PaymentResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.payments.entity.PaymentType;
import com.kkubeurakko.kkubeurakko_be.domain.payments.entity.Payments;
import com.kkubeurakko.kkubeurakko_be.domain.payments.repository.PaymentRepository;
import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.PortOnePaymentResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.portone.service.PortOneService;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.repository.UserRepository;
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

	private final UserRepository userRepository;
	private final CartRepository cartRepository;
	private final UserCouponRepository userCouponRepository;
	private final PaymentRepository paymentRepository;
	private final AddressRepository addressRepository;
	private final SimpMessagingTemplate messagingTemplate;
	private final OrderRepository orderRepository;
	private final UserService userService;
	private final AesUtil aesUtil;
	private final PortOneService portOneService;
	private final ApplicationEventPublisher eventPublisher;
	private final OrderNumberUtil orderNumberUtil;

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
					.build();
			})
			.toList();
	}

	@Transactional
	@Override
	public Long createOrder(OrderCreateRequest request, CustomOAuth2User customOAuth2User) {

		try {
			Users user = userService.getUser(customOAuth2User.getName());
			Cart cart = cartRepository.findByUserNumber(user.getUserNumber())
				.orElseThrow(() -> new IllegalStateException("장바구니가 비어있습니다."));

			if (cart.getCartItemList().isEmpty()) {
				throw new IllegalStateException("장바구니에 담긴 메뉴가 없습니다.");
			}

			// Stores store = cart.getCartItemList().get(0)
			// 	.getMenu()
			// 	.getMenuGroup()
			// 	.getStore();
			//
			// if (!store.isOpen()) {
			// 	throw new IllegalStateException("현재 가게가 영업 중이 아닙니다.");
			// }

			Addresses address = null;
			if (request.orderType() == OrderType.DELIVERY) {
				address = addressRepository.findById(request.addressId())
					.orElseThrow(() -> new EntityNotFoundException("주소를 찾을 수 없습니다."));
			}

			int discountAmount = applyCouponIfNecessary(user.getUserNumber(), cart, request);
			int totalAmount = calculateCartTotal(cart);

			String orderNumber = orderNumberUtil.createOrderNumber(1L, request.orderType());
			Orders order = request.toOrderEntity(user, address, totalAmount, orderNumber);
			Orders savedOrder = orderRepository.save(order);

			createOrderItems(order, cart);

			PortOnePaymentResponseDto paymentResponse =
				portOneService.getPortOnePayment(request.paymentId());

			Payments payment = toPaymentEntity(order, discountAmount, paymentResponse);
			paymentRepository.save(payment);

			cart.clearItems();

			sendOrderToStore(order);

			return savedOrder.getId();

		} catch (Exception e) {
			OrderCreateException exception =
				new OrderCreateException(
					"결제는 자동으로 취소 됩니다.\n" +
						"환불이 지연되거나 같은 문제가 지속될 경우 가게로 문의 부탁드립니다.",
					request.paymentId(),
					e
				);
			eventPublisher.publishEvent(exception);
			throw exception;
		}
	}

	@Override
	public void acceptOrder(Long orderId, int completionTime) {

	}

	@Override
	public void startCooking(Long orderId) {

	}

	@Override
	public void completeOrder(Long orderId) {

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
			.phoneNum(aesUtil.decryptFromBase64(order.getUser().getPhone()))
			.orderMenuList(orderMenuList)
			.payment(PaymentResponseDto.from(order.getPayments()))
			.build();
	}

	private void createOrderItems(Orders order, Cart cart) {
		for (CartItem cartItem : cart.getCartItemList()) {

			OrderItems orderItem = toOrderItemEntity(order, cartItem);

			for (CartItemOption opt : cartItem.getOptionList()) {
				orderItem.getOrderItemOptionList()
					.add(toOrderItemOptionEntity(orderItem, opt));
			}

			order.getOrderItemList().add(orderItem);
		}
	}

	private OrderItems toOrderItemEntity(Orders order, CartItem cartItem) {
		int optionTotalPrice = cartItem.getOptionList().stream()
			.mapToInt(option -> option.getOption().getPrice())
			.sum();

		int totalPrice =
			cartItem.getQuantity() *
				(cartItem.getMenu().getPrice() + optionTotalPrice);
		return OrderItems.builder()
			.order(order)
			.menu(cartItem.getMenu())
			.quantity(cartItem.getQuantity())
			.totalPrice(totalPrice)
			.build();
	}

	private OrderItemsOptions toOrderItemOptionEntity(OrderItems orderItem, CartItemOption opt) {
		return OrderItemsOptions.builder()
			.orderItems(orderItem)
			.option(opt.getOption())
			.build();
	}

	private Payments toPaymentEntity(
		Orders order,
		int discountAmount,
		PortOnePaymentResponseDto paymentResponseDto
	) {
		return Payments.builder()
			.order(order)
			.paymentsType(getPaymentType(paymentResponseDto))
			.totalAmount(order.getTotalAmount())
			.finalAmount(paymentResponseDto.amount().total())
			.discountAmount(discountAmount)
			.transactionId(paymentResponseDto.transactionId())
			.build();
	}

	private PaymentType getPaymentType(PortOnePaymentResponseDto paymentResponseDto) {
		String type = paymentResponseDto.method().type();

		if ("PaymentMethodCard".equals(type)) {
			return PaymentType.CARD;
		} else if ("PaymentMethodConvenienceStore".equals(type)) {
			return PaymentType.CONVENIENCE_STORE;
		} else if ("PaymentMethodEasyPay".equals(type)) {
			return PaymentType.EASY_PAY;
		} else if ("PaymentMethodGiftCertificate".equals(type)) {
			return PaymentType.GIFT_CERTIFICATE;
		} else if ("PaymentMethodMobile".equals(type)) {
			return PaymentType.MOBILE;
		} else if ("PaymentMethodTransfer".equals(type)) {
			return PaymentType.TRANSFER;
		} else if ("PaymentMethodVirtualAccount".equals(type)) {
			return PaymentType.VIRTUAL_ACCOUNT;
		} else {
			return PaymentType.GENERAL;
		}
	}

	private int calculateCartTotal(Cart cart) {
		return cart.getCartItemList().stream()
			.mapToInt(i -> i.getMenu().getPrice() * i.getQuantity()
				+ i.getOptionList().stream().mapToInt(o -> i.getQuantity() * o.getOption().getPrice()).sum())
			.sum();
	}

	private int applyCouponIfNecessary(String userNumber, Cart cart, OrderCreateRequest request) {

		if (request.couponNumber() == null)
			return 0;

		UsersCoupons usersCoupon = userCouponRepository.findByUser_UserNumberAndCouponsNumber(userNumber,
				request.couponNumber())
			.orElseThrow(() -> new IllegalArgumentException("쿠폰이 존재하지 않습니다."));

		Coupons coupon = usersCoupon.getCoupon();

		if (usersCoupon.isUsed())
			throw new IllegalArgumentException("이미 사용된 쿠폰입니다.");

		int orderAmount = calculateCartTotal(cart);
		if (orderAmount < coupon.getMinOrderAmount())
			throw new IllegalStateException("최소 주문 금액에 미달합니다.");

		usersCoupon.use();

		return coupon.getDiscountAmount();
	}

	private void sendOrderToStore(Orders order) {
		messagingTemplate.convertAndSend(
			"/topic/store/" + order.getUser().getUserNumber(),
			OrderSocketResponse.from(order)
		);
	}
}
