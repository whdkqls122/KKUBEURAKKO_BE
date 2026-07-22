package com.kkubeurakko.kkubeurakko_be.domain.stores.entity;

import java.time.DayOfWeek;

public enum BusinessDay {
	MONDAY,
	TUESDAY,
	WEDNESDAY,
	THURSDAY,
	FRIDAY,
	SATURDAY,
	SUNDAY,
	ALLDAY;

	public static BusinessDay from(DayOfWeek dayOfWeek) {
		return BusinessDay.valueOf(dayOfWeek.name());
	}
}