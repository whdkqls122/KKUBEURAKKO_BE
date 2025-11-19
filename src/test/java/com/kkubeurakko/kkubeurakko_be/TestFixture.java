package com.kkubeurakko.kkubeurakko_be;

import java.time.LocalDate;

import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuGroups;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuStatus;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.Menus;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroupType;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroups;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionStatus;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.Options;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;

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
}