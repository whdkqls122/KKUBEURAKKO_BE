package com.kkubeurakko.kkubeurakko_be.domain.cart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kkubeurakko.kkubeurakko_be.domain.cart.entity.Cart;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
	Optional<Cart> findByUser(Users user);

	@Query("select c from Cart c where c.user.userNumber = :userNumber ")
	Cart findByUserNumber(@Param("userNumber") String userNumber);
}
