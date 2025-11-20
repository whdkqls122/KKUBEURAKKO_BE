package com.kkubeurakko.kkubeurakko_be.domain.cart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kkubeurakko.kkubeurakko_be.domain.cart.entity.Cart;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;

public interface CartRepository extends JpaRepository<Cart, Long> {
	Optional<Cart> findByUser(Users user);
}
