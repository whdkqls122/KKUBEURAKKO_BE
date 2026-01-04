package com.kkubeurakko.kkubeurakko_be.domain.stores.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.BusinessDay;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.StoreBusinessHours;

public interface StoreBusinessHoursRepository
	extends JpaRepository<StoreBusinessHours, Long> {

	void deleteByStoreId(Long storeId);

	Optional<StoreBusinessHours> findByStoreIdAndDayOfWeek(
		Long storeId,
		BusinessDay dayOfWeek
	);

}