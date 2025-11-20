package com.kkubeurakko.kkubeurakko_be.domain.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kkubeurakko.kkubeurakko_be.domain.cart.entity.CartItemOption;

public interface CartItemOptionRepository extends JpaRepository<CartItemOption, Long> {
}
