package com.kkubeurakko.kkubeurakko_be.domain.stores.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.DeliveryRegion;

public interface DeliveryRegionRepository extends JpaRepository<DeliveryRegion, Long> {
	Optional<DeliveryRegion> findByStoreId(Long storeId);

	Boolean existsByStoreId(Long storeId);
}