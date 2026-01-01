package com.kkubeurakko.kkubeurakko_be;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.kkubeurakko.kkubeurakko_be.domain.addresses.entity.AddressCategory;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.entity.Addresses;
import com.kkubeurakko.kkubeurakko_be.domain.cart.entity.Cart;
import com.kkubeurakko.kkubeurakko_be.domain.cart.entity.CartItem;
import com.kkubeurakko.kkubeurakko_be.domain.cart.entity.CartItemOption;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.CouponType;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.Coupons;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.UsersCoupons;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuGroups;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuStatus;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.Menus;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroupType;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroups;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionStatus;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.Options;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderAddressSnapshot;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderItems;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderItemsOptions;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderType;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Orders;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Status;
import com.kkubeurakko.kkubeurakko_be.domain.payments.entity.PaymentType;
import com.kkubeurakko.kkubeurakko_be.domain.payments.entity.Payments;
import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.PaymentAmount;
import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.PaymentMethodDto;
import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.PortOnePaymentResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.UserRole;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;

public class TestFixture {

	public static Stores createStore() {
		return Stores.builder()
			.id(1L)
			.isOpen(true)
			.minOrderPrice(10000)
			.deliveryAvgTime(60)
			.packagingAvgTime(10)
			.build();
	}

	public static MenuGroups createMenuGroup(Long id, Stores store) {
		MenuGroups group = MenuGroups.builder()
			.id(id)
			.name("대표메뉴")
			.description("대표 메뉴 설명")
			.menuStatus(MenuStatus.ON_SALE)
			.store(store)
			.build();

		return group;
	}

	public static Menus createMenu(Long id, MenuGroups group) {
		Menus menu = Menus.builder()
			.id(id)
			.name("양념꾸브")
			.description("맛있는 양념꾸브")
			.price(18000)
			.img("img.png")
			.menuStatus(MenuStatus.ON_SALE)
			.menuGroup(group)
			.build();

		group.getMenus().add(menu);

		return menu;
	}

	public static Options createOption(Long id, String name, int price, OptionGroups optionGroup) {
		Options option = Options.builder()
			.id(id)
			.name(name)
			.price(price)
			.optionStatus(OptionStatus.ON_SALE)
			.optionGroup(optionGroup)
			.build();

		optionGroup.getOptions().add(option);

		return option;
	}

	public static OptionGroups createOptionGroup(Long id) {
		return OptionGroups.builder()
			.id(id)
			.name("맵기 선택")
			.description("맵기를 선택하세요")
			.required(true)
			.selectedNum(1)
			.optionGroupType(OptionGroupType.SINGLE)
			.optionStatus(OptionStatus.ON_SALE)
			.build();
	}

	public static Users createUser(String userNumber, String phone) {
		return Users.builder()
			.userNumber(userNumber)
			.role(UserRole.CUSTOMER)
			.email("test@naver.com")
			.nickname("조아빈")
			.phone(phone)
			.build();
	}

	public static OrderItemsOptions createOrderItemOption(Long id, Options option, OrderItems parent) {
		return OrderItemsOptions.builder()
			.id(id)
			.option(option)
			.orderItems(parent)
			.build();
	}

	public static OrderItems createOrderItem(Long id, Menus menu, List<Options> options, Orders parent) {

		OrderItems item = OrderItems.builder()
			.id(id)
			.menu(menu)
			.quantity(2)
			.totalPrice(menu.getPrice() * 2)
			.order(parent)
			.orderItemOptionList(new ArrayList<>())
			.build();

		List<OrderItemsOptions> optionEntities = options.stream()
			.map(opt -> OrderItemsOptions.builder()
				.id(opt.getId())
				.option(opt)
				.orderItems(item)
				.build()
			).toList();

		item.getOrderItemOptionList().addAll(optionEntities);

		return item;
	}

	public static Cart createCartWithOneItem(Users user) {
		Menus menu = TestFixture.createMenu(1L,
			TestFixture.createMenuGroup(1L, TestFixture.createStore())
		);

		Cart cart = Cart.builder()
			.id(1L)
			.user(user)
			.cartItemList(new ArrayList<>())
			.build();

		CartItem cartItem = CartItem.builder()
			.id(1L)
			.cart(cart)
			.menu(menu)
			.quantity(2)
			.optionList(new ArrayList<>())
			.build();

		cart.getCartItemList().add(cartItem);

		return cart;
	}

	public static CartItem createCartItemWithOption(Cart cart, Menus menu, Options option) {
		CartItem cartItem = CartItem.builder()
			.id(1L)
			.cart(cart)
			.menu(menu)
			.quantity(1)
			.optionList(new ArrayList<>())
			.build();

		CartItemOption cartItemOption = CartItemOption.builder()
			.id(1L)
			.cartItem(cartItem)
			.option(option)
			.build();

		cartItem.getOptionList().add(cartItemOption);
		return cartItem;
	}

	public static CartItem createCartItem(Cart cart, Menus menu, int quantity) {
		return CartItem.builder()
			.id(1L)
			.cart(cart)
			.menu(menu)
			.quantity(quantity)
			.optionList(new ArrayList<>())
			.build();
	}

	public static Addresses createAddress(Long id, Users user) {
		return Addresses.builder()
			.id(id)
			.addressName("우리집")
			.roadName("서울 강남구 테헤란로")
			.detailAddress("101호")
			.postalCode("06236")
			.isPrimary(true)
			.category(AddressCategory.HOME)
			.requestContent("문 앞에 놔주세요")
			.entrancePassword("1234#")
			.directionsInfo("초록색 문")
			.user(user)
			.build();
	}

	public static Orders createOrderWithPayment(
		Long id,
		Users user,
		List<Menus> menus,
		List<List<Options>> optionsPerMenu
	) {
		Orders order = Orders.builder()
			.id(id)
			.user(user)
			.orderItemList(new ArrayList<>())
			.orderType(OrderType.DELIVERY)
			.status(Status.COMPLETED)
			.completionTime(20)
			.orderDate(LocalDateTime.now())
			.addressSnapshot(OrderAddressSnapshot.builder()
				.addressName("우리집")
				.roadName("서울시 강남구 테헤란로")
				.detailAddress("101동 1004호")
				.requestContent("문앞에 두세요")
				.entrancePassword("1234#")
				.directionsInfo("엘리베이터 오른쪽")
				.build())
			.build();

		for (int i = 0; i < menus.size(); i++) {
			OrderItems item = createOrderItem(
				id + 100 + i,
				menus.get(i),
				optionsPerMenu.get(i),
				order
			);
			order.getOrderItemList().add(item);
		}

		// Payment 생성
		Payments payment = Payments.builder()
			.id(id + 1000)
			.order(order)
			.totalAmount(order.getOrderItemList().stream().mapToInt(OrderItems::getTotalPrice).sum())
			.paymentsType(PaymentType.CARD)
			.discountAmount(2000)
			.finalAmount(order.getOrderItemList().stream().mapToInt(OrderItems::getTotalPrice).sum() - 2000)
			.transactionId("T" + id + "PAY")
			.build();

		try {
			java.lang.reflect.Field paymentField = Orders.class.getDeclaredField("payments");
			paymentField.setAccessible(true);
			paymentField.set(order, payment);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return order;
	}

	public static Coupons createCoupon(Long id, String name, int quantity, LocalDate validFrom, LocalDate validUntil,
		CouponType couponType, com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.Status status, Stores store) {
		return Coupons.builder()
			.id(id)
			.name(name)
			.discountAmount("1000원") // 예시
			.minOrderAmount(12000)
			.couponType(couponType)
			.validFrom(validFrom)
			.validUntil(validUntil)
			.quantity(quantity)
			.status(status)
			.store(store)
			.build();
	}

	public static UsersCoupons createUserCoupon(Long id, Users user, Coupons coupon, boolean isUsed) {
		return UsersCoupons.builder()
			.id(id)
			.user(user)
			.coupon(coupon)
			.couponsNumber(UUID.randomUUID().toString().replace("-", "").substring(0, 19)) // 19자리
			.isUsed(isUsed)
			.build();
	}

	public static PortOnePaymentResponseDto createPortOnePaymentResponse(
		String paymentId,
		String transactionId,
		int totalAmount
	) {
		return new PortOnePaymentResponseDto(
			"PAID",
			paymentId,
			transactionId,
			"store-test-001",
			new PaymentAmount(
				totalAmount,
				0,
				totalAmount / 10,
				totalAmount - (totalAmount / 10),
				0
			),
			new PaymentMethodDto(
				"PaymentMethodCard",
				Map.of()
			)
		);
	}
}