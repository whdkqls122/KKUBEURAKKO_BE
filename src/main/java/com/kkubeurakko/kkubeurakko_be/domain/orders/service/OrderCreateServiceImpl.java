package com.kkubeurakko.kkubeurakko_be.domain.orders.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
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
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderCreateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderCreatedEvent;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderItems;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderItemsOptions;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderType;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Orders;
import com.kkubeurakko.kkubeurakko_be.domain.orders.repository.OrderRepository;
import com.kkubeurakko.kkubeurakko_be.domain.orders.util.OrderNumberUtil;
import com.kkubeurakko.kkubeurakko_be.domain.payments.entity.PaymentType;
import com.kkubeurakko.kkubeurakko_be.domain.payments.entity.Payments;
import com.kkubeurakko.kkubeurakko_be.domain.payments.exception.InvalidPaymentException;
import com.kkubeurakko.kkubeurakko_be.domain.payments.repository.PaymentRepository;
import com.kkubeurakko.kkubeurakko_be.domain.payments.service.PaymentAttemptService;
import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.PortOnePaymentResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.portone.exception.PortOnePaymentHistoryException;
import com.kkubeurakko.kkubeurakko_be.domain.portone.service.PortOneService;
import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.DeliveryRegionAbleResponse;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.StoreStatus;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;
import com.kkubeurakko.kkubeurakko_be.domain.stores.service.DeliveryRegionService;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.service.UserService;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCreateServiceImpl implements OrderCreateService {
	private final UserCouponRepository userCouponRepository;
	private final PaymentRepository paymentRepository;
	private final AddressRepository addressRepository;
	private final OrderRepository orderRepository;
	private final UserService userService;
	private final ApplicationEventPublisher eventPublisher;
	private final OrderNumberUtil orderNumberUtil;
	private final DeliveryRegionService deliveryRegionService;
	private final PaymentAttemptService paymentAttemptService;

	/**
	 * 결제 확인이 끝난 주문의 장바구니, 주소,금액을 검증하고 주문과 결제를 저장
	 * 외부 API를 호출하지 않으며, 결제 처리 기록 완료까지 하나의 트랜잭션으로 커밋
	 * 실패(커밋 시점 실패 포함) 시 결제 취소 요청은 호출한 Facade가 처리
	 */
	@Transactional
	@Override
	public Long createOrder(
		OrderCreateRequest request,
		CustomOAuth2User customOAuth2User,
		PortOnePaymentResponseDto paymentResponse
	) {
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

		validatePaymentAmount(paymentResponse, finalAmount);

		String orderNumber = orderNumberUtil.createOrderNumber(store.getId(), request.orderType());
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

		Payments payment = toPaymentEntity(order, discountAmount, request.paymentId(), paymentResponse);
		Orders savedOrder = saveOrderAndPayment(order, payment);

		processAfterOrderSaved(user, request, savedOrder.getId(), store.getId(), address);

		// 주문 저장과 같은 트랜잭션에서 결제 처리 기록을 완료로 바꾼다 (이미 취소 요청됐으면 예외로 롤백)
		paymentAttemptService.complete(request.paymentId());

		return savedOrder.getId();
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
		String paymentId,
		PortOnePaymentResponseDto paymentResponseDto
	) {
		return Payments.builder()
			.order(order)
			.paymentsType(getPaymentType(paymentResponseDto))
			.totalAmount(order.getTotalAmount())
			.finalAmount(paymentResponseDto.amount().total())
			.discountAmount(discountAmount)
			.transactionId(paymentResponseDto.transactionId())
			.paymentId(paymentId)
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

	private void validatePaymentAmount(PortOnePaymentResponseDto paymentResponse, int expectedFinalAmount) {
		if (paymentResponse.amount().total() != expectedFinalAmount) {
			// 결제는 완료됐으므로 환불 대상 예외
			throw new IllegalStateException("결제 금액이 일치하지 않습니다.");
		}
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