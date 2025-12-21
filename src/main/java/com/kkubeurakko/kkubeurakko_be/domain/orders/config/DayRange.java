package com.kkubeurakko.kkubeurakko_be.domain.orders.config;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DayRange {
	public static LocalDateTime DayStart(LocalDate date) {
		return date.atStartOfDay();
	}

	public static LocalDateTime DayEnd(LocalDate date) {
		return date.atTime(23, 59, 59);
	}
}
