package com.kkubeurakko.kkubeurakko_be;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuGroups;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuStatus;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.Menus;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroupType;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroups;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionStatus;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.Options;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderItems;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderItemsOptions;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderType;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Orders;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Status;
import com.kkubeurakko.kkubeurakko_be.domain.payments.entity.PaymentType;
import com.kkubeurakko.kkubeurakko_be.domain.payments.entity.Payments;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.UserRole;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;

public class TestFixture {

	public static Stores createStore() {
		return Stores.builder()
			.id(1L)
			.address("꾸브라꼬 경기 안양점")
			.tel("02-000-0000")
			.openTime(LocalDate.of(2025, 1, 1))
			.closeTime(LocalDate.of(2025, 12, 31))
			.isOpen(true)
			.minOrderPrice(10000)
			.delivery_region("안양시")
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

	public static Orders createOrderWithPayment(Long id, Users user, List<Menus> menus,
		List<List<Options>> optionsPerMenu) {
		Orders order = Orders.builder()
			.id(id)
			.user(user)
			.orderItemList(new ArrayList<>())
			.orderType(OrderType.DELIVERY)
			.status(Status.COMPLETED)
			.completionTime(20)
			.address("서울시 강남구")
			.orderDate(LocalDateTime.now())
			.build();

		// OrderItems 생성 및 추가
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
			.paymentsType(PaymentType.GENERAL)
			.transactionId("T" + id + "PAY")
			.build();

		// Builder 사용으로 Orders에 Payment 포함
		// Orders 엔티티에 payments 필드가 final이나 builder 미포함이면, OrderService 테스트에서는 reflection 또는 getter를 통한 세팅 필요
		try {
			java.lang.reflect.Field paymentField = Orders.class.getDeclaredField("payments");
			paymentField.setAccessible(true);
			paymentField.set(order, payment);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return order;
	}
}