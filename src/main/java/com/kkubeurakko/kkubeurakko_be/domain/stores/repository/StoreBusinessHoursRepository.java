package com.kkubeurakko.kkubeurakko_be.domain.stores.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.StoreBusinessHours;

public interface StoreBusinessHoursRepository
	extends JpaRepository<StoreBusinessHours, Long> {

	void deleteByStoreId(Long storeId);
}