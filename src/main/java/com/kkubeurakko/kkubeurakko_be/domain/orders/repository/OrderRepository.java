package com.kkubeurakko.kkubeurakko_be.domain.orders.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Orders;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long> {
	List<Orders> findByUser(Users user);

	@Query("""
		    SELECT DISTINCT o FROM Orders o
		    JOIN FETCH o.orderItemList oi
		    JOIN FETCH oi.orderItemOptionList oio
		    WHERE o.id = :orderId AND o.user = :user
		""")
	Optional<Orders> findByIdAndUser(Long orderId, Users user);
}
