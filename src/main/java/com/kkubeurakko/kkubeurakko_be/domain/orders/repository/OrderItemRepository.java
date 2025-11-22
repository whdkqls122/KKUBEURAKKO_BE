package com.kkubeurakko.kkubeurakko_be.domain.orders.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderItems;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItems, Long> {

}
