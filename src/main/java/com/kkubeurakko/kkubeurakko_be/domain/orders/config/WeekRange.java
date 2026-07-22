package com.kkubeurakko.kkubeurakko_be.domain.orders.config;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class WeekRange {

	public static LocalDateTime getWeekStart(LocalDate date) {
		return date.with(DayOfWeek.MONDAY).atStartOfDay();
	}

	public static LocalDateTime getWeekEnd(LocalDate date) {
		return date.with(DayOfWeek.SUNDAY).atTime(23, 59, 59);
	}
}
