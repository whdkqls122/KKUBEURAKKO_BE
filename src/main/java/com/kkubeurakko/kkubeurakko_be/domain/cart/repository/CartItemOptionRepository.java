package com.kkubeurakko.kkubeurakko_be.domain.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kkubeurakko.kkubeurakko_be.domain.cart.entity.CartItemOption;

@Repository
public interface CartItemOptionRepository extends JpaRepository<CartItemOption, Long> {
}
