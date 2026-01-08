package com.kkubeurakko.kkubeurakko_be.domain.stores.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.DeliveryRegion;

public interface DeliveryRegionRepository extends JpaRepository<DeliveryRegion, Long> {
}