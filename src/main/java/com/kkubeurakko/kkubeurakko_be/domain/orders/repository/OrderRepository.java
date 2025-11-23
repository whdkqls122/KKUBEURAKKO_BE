package com.kkubeurakko.kkubeurakko_be.domain.orders.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Orders;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long> {
	List<Orders> findByUser(Users user);
}
