package com.kkubeurakko.kkubeurakko_be.domain.stores.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;

@Repository
public interface StoreRepository extends JpaRepository<Stores, Long> {
}
