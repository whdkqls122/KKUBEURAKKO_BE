package com.kkubeurakko.kkubeurakko_be.domain.orders.service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
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
import com.kkubeurakko.kkubeurakko_be.domain.orders.exception.OrderCreateException;
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
	private final PortOneService portOneService;
	private final ApplicationEventPublisher eventPublisher;
	private final OrderNumberUtil orderNumberUtil;
	private final DeliveryRegionService deliveryRegionService;
	private final PaymentAttemptService paymentAttemptService;
	private final RedisTemplate<String, String> redisTemplate;

	/**
	 * 결제 확인 후 장바구니·주소·금액을 검증하고 주문과 결제를 저장한다.
	 * 결제 확인 이후 실패하면 결제 취소 이벤트를 발행한다.
	 */
	@Transactional
	@Override
	public Long createOrder(OrderCreateRequest request, CustomOAuth2User customOAuth2User) {
		// 결제 확인 전 실패(중복, 처리 중, 락 오류, 조회 실패, 미완료 결제)는 취소 대상이 아니므로 롤백 이벤트 없이 그대로 던짐
		// 조회 실패처럼 결제 여부를 알 수 없는 경우는 처리 기록이 남아 방치 감지 스케줄러가 취소한다
		PortOnePaymentResponseDto paymentResponse = getUsablePayment(request);
		try {
			// 여기부터 실패하면 우리 상점에서 완료된 결제이므로 환불 대상
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
			issueCashReceiptIfRequested(user, request, finalAmount);

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
		} catch (Exception e) {
			OrderCreateException exception =
				new OrderCreateException(
					e.getMessage() + "\n\n" +
						"결제는 잠시 후 자동으로 취소됩니다.\n" +
						"환불이 지연되거나 같은 문제가 지속될 경우 가게로 문의 부탁드립니다.",
					request.paymentId(),
					request.isCashReceipt(),
					e
				);
			eventPublisher.publishEvent(exception);
			throw exception;
		}
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

	/**
	 * 이미 사용된 결제인지 확인하고 결제 처리 기록을 남긴 뒤 결제 상태·상점을 확인한다.
	 * 실패 시 결제 취소 이벤트를 발행하지 않는다.
	 */
	private PortOnePaymentResponseDto getUsablePayment(OrderCreateRequest request) {
		String paymentId = request.paymentId();
		if (paymentRepository.existsByPaymentId(paymentId)) {
			throw new InvalidPaymentException("이미 사용된 결제입니다.");
		}
		// 주문 처리 전에 기록을 먼저 커밋: 같은 결제의 동시 요청을 막고, 이후 주문이 완료되지 않으면 취소 대상으로 남긴다
		paymentAttemptService.start(paymentId, request.isCashReceipt());
		try {
			return portOneService.getPaidPayment(paymentId);
		} catch (InvalidPaymentException e) {
			// 완료되지 않았거나 우리 상점 결제가 아니면 취소할 결제가 없으므로, 결제 완료 후 다시 시도할 수 있게 기록을 지운다
			paymentAttemptService.discard(paymentId);
			throw e;
		} catch (PortOnePaymentHistoryException e) {
			// 결제 여부를 알 수 없으므로 기록을 남겨 방치 감지 스케줄러가 확인 후 취소하게 하고, 새 결제로 다시 주문하도록 안내한다
			throw new PortOnePaymentHistoryException(
				"결제 확인에 실패했습니다. 결제가 완료됐다면 잠시 후 자동으로 취소되니 다시 결제해 주세요.", e);
		}
	}

	private void validatePaymentAmount(PortOnePaymentResponseDto paymentResponse, int expectedFinalAmount) {
		if (paymentResponse.amount().total() != expectedFinalAmount) {
			// 결제는 완료됐으므로 환불 대상 예외
			throw new IllegalStateException("결제 금액이 일치하지 않습니다.");
		}
	}

	private void issueCashReceiptIfRequested(Users user, OrderCreateRequest request, int finalAmount) {
		if (request.isCashReceipt()) {
			portOneService.issueCashReceipt(
				user,
				request.paymentId(),
				request.cashReceiptRequest(),
				finalAmount
			);
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