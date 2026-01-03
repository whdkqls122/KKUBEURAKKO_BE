package com.kkubeurakko.kkubeurakko_be.domain.cart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kkubeurakko.kkubeurakko_be.domain.cart.entity.Cart;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
	@Query("select c from Cart c where c.user.userNumber = :userNumber ")
	Optional<Cart> findByUserNumber(@Param("userNumber") String userNumber);
}
