package com.kkubeurakko.kkubeurakko_be.domain.orders.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

public class MonthRange {

	public static LocalDateTime getMonthStart(LocalDate date) {
		return YearMonth.from(date)
			.atDay(1)
			.atStartOfDay();
	}

	public static LocalDateTime getMonthEnd(LocalDate date) {
		return YearMonth.from(date)
			.atEndOfMonth()
			.atTime(23, 59, 59);
	}
}
