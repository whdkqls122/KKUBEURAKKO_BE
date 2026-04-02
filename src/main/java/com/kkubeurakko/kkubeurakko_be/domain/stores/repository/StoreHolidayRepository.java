package com.kkubeurakko.kkubeurakko_be.domain.stores.repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.HolidayType;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.StoreHoliday;

public interface StoreHolidayRepository extends JpaRepository<StoreHoliday, Long> {

	List<StoreHoliday> findByStoreId(Long storeId);

	boolean existsByStoreIdAndHolidayTypeAndDayOfWeek(
		Long storeId,
		HolidayType holidayType,
		DayOfWeek dayOfWeek
	);

	boolean existsByStoreIdAndHolidayTypeAndHolidayDate(
		Long storeId,
		HolidayType holidayType,
		LocalDate holidayDate
	);
	
	void deleteByStoreIdAndHolidayTypeAndHolidayDate(
		Long storeId,
		HolidayType holidayType,
		LocalDate holidayDate
	);

	void deleteByStoreIdAndHolidayTypeAndDayOfWeek(
		Long storeId,
		HolidayType holidayType,
		DayOfWeek dayOfWeek
	);
}
