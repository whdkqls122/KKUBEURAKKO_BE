package com.kkubeurakko.kkubeurakko_be;

import java.time.LocalDate;

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
}