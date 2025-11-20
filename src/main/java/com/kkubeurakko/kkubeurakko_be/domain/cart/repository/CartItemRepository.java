package com.kkubeurakko.kkubeurakko_be.domain.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kkubeurakko.kkubeurakko_be.domain.cart.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
