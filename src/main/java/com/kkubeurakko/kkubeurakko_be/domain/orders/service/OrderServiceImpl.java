package com.kkubeurakko.kkubeurakko_be.domain.orders.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kkubeurakko.kkubeurakko_be.domain.addresses.entity.Addresses;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.repository.AddressRepository;
import com.kkubeurakko.kkubeurakko_be.domain.cart.entity.Cart;
import com.kkubeurakko.kkubeurakko_be.domain.cart.entity.CartItem;
import com.kkubeurakko.kkubeurakko_be.domain.cart.entity.CartItemOption;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.Coupons;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.UsersCoupons;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.repository.UserCouponRepository;
import com.kkubeurakko.kkubeurakko_be.domain.orders.config.OrderSocketSender;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderAddressResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderCreateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderCreatedEvent;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderDetailResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderItemsOptionResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderMenuResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderSocketResponse;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderItems;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderItemsOptions;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderType;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Orders;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Status;
import com.kkubeurakko.kkubeurakko_be.domain.orders.exception.OrderCreateException;
import com.kkubeurakko.kkubeurakko_be.domain.orders.repository.OrderRepository;
import com.kkubeurakko.kkubeurakko_be.domain.orders.util.OrderNumberUtil;
import com.kkubeurakko.kkubeurakko_be.domain.payments.dto.PaymentResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.payments.entity.PaymentType;
import com.kkubeurakko.kkubeurakko_be.domain.payments.entity.Payments;
import com.kkubeurakko.kkubeurakko_be.domain.payments.repository.PaymentRepository;
import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.PortOnePaymentResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.portone.service.PortOneService;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.DeliveryRegionAbleResponse;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.BusinessDay;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.StoreBusinessHours;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.StoreStatus;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.StoreBusinessHoursRepository;
import com.kkubeurakko.kkubeurakko_be.domain.stores.service.DeliveryRegionService;
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
	private final UserCouponRepository userCouponRepository;
	private final PaymentRepository paymentRepository;
	private final AddressRepository addressRepository;
	private final OrderRepository orderRepository;
	private final UserService userService;
	private final AesUtil aesUtil;
	private final PortOneService portOneService;
	private final ApplicationEventPublisher eventPublisher;
	private final OrderNumberUtil orderNumberUtil;
	private final OrderSocketSender orderSocketSender;
	private final OrderDecryptService orderDecryptService;
	private final StoreBusinessHoursRepository storeBusinessHoursRepository;
	private final DeliveryRegionService deliveryRegionService;

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

	@Transactional
	@Override
	public Long createOrder(OrderCreateRequest request, CustomOAuth2User customOAuth2User) {

		try {
			Users user = getUser(customOAuth2User);
			Cart cart = validateCart(user);
			Stores store = extractStore(cart);

			if (store.getStatus() != StoreStatus.OPEN) {
				throw new IllegalStateException("현재 가게가 영업 중이 아닙니다.");
			}

			Addresses address = getAddress(store, request, user);

			int totalAmount = calculateCartTotal(cart);
			int discountAmount = calculateDiscountAmount(user.getUserNumber(), cart, request);
			int finalAmount = totalAmount - discountAmount;

			String orderNumber = orderNumberUtil.createOrderNumber(store.getId(), request.orderType());

			PortOnePaymentResponseDto paymentResponse =
				verifyPaymentAndIssueCashReceipt(user, request, finalAmount);

			UsersCoupons usersCoupon = getUsersCouponIfExists(user, request);

			Orders order = createOrderEntity(
				request,
				user,
				address,
				totalAmount,
				orderNumber,
				cart,
				usersCoupon
			);

			Payments payment = toPaymentEntity(order, discountAmount, paymentResponse);
			Orders savedOrder = saveOrderAndPayment(order, payment);

			processAfterOrderSaved(user, request, savedOrder.getId(), store.getId(), address);

			return savedOrder.getId();
		} catch (Exception e) {
			OrderCreateException exception =
				new OrderCreateException(
					e.getMessage() + "\n\n" +
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

	private int calculateDiscountAmount(String userNumber, Cart cart, OrderCreateRequest request) {
		if (request.couponNumber() == null) {
			return 0;
		}

		UsersCoupons usersCoupon = userCouponRepository
			.findByUser_UserNumberAndCouponsNumber(userNumber, request.couponNumber())
			.orElseThrow(() -> new IllegalArgumentException("쿠폰이 존재하지 않습니다."));

		Coupons coupon = usersCoupon.getCoupon();

		if (usersCoupon.isUsed()) {
			throw new IllegalArgumentException("이미 사용된 쿠폰입니다.");
		}

		int orderAmount = calculateCartTotal(cart);
		if (orderAmount < coupon.getMinOrderAmount()) {
			throw new IllegalStateException("최소 주문 금액에 미달합니다.");
		}

		return calculateCouponDiscount(orderAmount, coupon);
	}

	private int calculateCouponDiscount(int orderAmount, Coupons coupon) {
		Integer discountAmount = coupon.getDiscountAmount();
		Integer discountRate = coupon.getDiscountRate();

		if (discountAmount != null && discountAmount > 0) {
			return discountAmount;
		}

		if (discountRate != null && discountRate > 0) {
			return orderAmount * discountRate / 100;
		}

		throw new IllegalStateException("쿠폰 할인 정보가 올바르지 않습니다.");
	}

	private void markCouponUsedIfNeeded(String userNumber, OrderCreateRequest request) {
		if (request.couponNumber() == null) {
			return;
		}

		UsersCoupons usersCoupon = userCouponRepository
			.findByUser_UserNumberAndCouponsNumber(userNumber, request.couponNumber())
			.orElseThrow(() -> new IllegalArgumentException("쿠폰이 존재하지 않습니다."));

		if (usersCoupon.isUsed()) {
			throw new IllegalArgumentException("이미 사용된 쿠폰입니다.");
		}

		usersCoupon.use();
	}

	private Users getUser(CustomOAuth2User customOAuth2User) {
		return userService.getUser(customOAuth2User.getName());
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

	private Stores extractStore(Cart cart) {
		return cart.getCartItemList().get(0)
			.getMenu()
			.getMenuGroup()
			.getStore();
	}

	private Cart validateCart(Users user) {
		Cart cart = user.getCart();

		if (cart == null) {
			throw new IllegalStateException("장바구니가 비어있습니다.");
		}
		if (cart.getCartItemList().isEmpty()) {
			throw new IllegalStateException("장바구니에 담긴 메뉴가 없습니다.");
		}

		return cart;
	}

	private Addresses getAddress(Stores store, OrderCreateRequest request, Users user) {
		if (request.orderType() != OrderType.DELIVERY) {
			return null;
		}

		Addresses addresses = addressRepository.findByIdAndUser(request.addressId(), user)
			.orElseThrow(() -> new EntityNotFoundException("사용자의 주소를 찾을 수 없습니다."));

		validateDeliveryRegion(store, addresses);

		return addresses;
	}

	private PortOnePaymentResponseDto verifyPaymentAndIssueCashReceipt(
		Users user,
		OrderCreateRequest request,
		int expectedFinalAmount
	) {
		PortOnePaymentResponseDto paymentResponse =
			portOneService.getPortOnePayment(request.paymentId());

		if (paymentResponse.amount().total() != expectedFinalAmount) {
			throw new IllegalStateException("결제 금액이 일치하지 않습니다.");
		}

		if (request.isCashReceipt()) {
			portOneService.issueCashReceipt(
				user,
				request.paymentId(),
				request.cashReceiptRequest(),
				expectedFinalAmount
			);
		}

		return paymentResponse;
	}

	private Orders createOrderEntity(
		OrderCreateRequest request,
		Users user,
		Addresses address,
		int totalAmount,
		String orderNumber,
		Cart cart,
		UsersCoupons usersCoupon
	) {
		Orders order = request.toOrderEntity(user, address, totalAmount, orderNumber, usersCoupon);
		createOrderItems(order, cart);
		return order;
	}

	private Orders saveOrderAndPayment(Orders order, Payments payment) {
		Orders savedOrder = orderRepository.save(order);
		paymentRepository.save(payment);
		return savedOrder;
	}

	private void processAfterOrderSaved(
		Users user,
		OrderCreateRequest request,
		Long orderId,
		Long storeId,
		Addresses addresses
	) {
		markCouponUsedIfNeeded(user.getUserNumber(), request);
		if (addresses != null) {
			addresses.useAddress();
		}
		user.updateCart(null);
		eventPublisher.publishEvent(new OrderCreatedEvent(orderId, storeId));
	}

	private void validateDeliveryRegion(Stores store, Addresses address) {
		DeliveryRegionAbleResponse isDeliverable = deliveryRegionService.isDeliverable(
			store.getId(),
			address.getLatitude(),
			address.getLongitude()
		);

		if (!isDeliverable.able()) {
			throw new IllegalStateException("배달 불가능한 지역입니다.");
		}
	}

	private UsersCoupons getUsersCouponIfExists(Users user, OrderCreateRequest request) {
		if (request.couponNumber() == null) {
			return null;
		}

		return userCouponRepository
			.findByUser_UserNumberAndCouponsNumber(
				user.getUserNumber(),
				request.couponNumber()
			)
			.orElseThrow(() -> new IllegalArgumentException("쿠폰이 존재하지 않습니다."));
	}
}
