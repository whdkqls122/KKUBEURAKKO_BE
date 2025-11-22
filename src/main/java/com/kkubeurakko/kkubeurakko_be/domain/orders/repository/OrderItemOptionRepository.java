package com.kkubeurakko.kkubeurakko_be.domain.orders.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderItemsOptions;

@Repository
public interface OrderItemOptionRepository extends JpaRepository<OrderItemsOptions, Long> {
}
