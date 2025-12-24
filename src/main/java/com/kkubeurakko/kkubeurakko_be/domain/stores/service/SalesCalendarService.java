package com.kkubeurakko.kkubeurakko_be.domain.stores.service;

import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.MonthlySalesCalendarResponseDto;

public interface SalesCalendarService {

	MonthlySalesCalendarResponseDto getMonthlySalesCalendar(int year, int month);
}
