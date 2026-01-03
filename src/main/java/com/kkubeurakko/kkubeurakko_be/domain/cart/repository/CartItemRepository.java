package com.kkubeurakko.kkubeurakko_be.domain.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kkubeurakko.kkubeurakko_be.domain.cart.entity.CartItem;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
	@Query("select sum(c.quantity) from CartItem c where c.cart.id = :cartId")
	Integer countCartItem(@Param("cartId") Long cartId);
}
