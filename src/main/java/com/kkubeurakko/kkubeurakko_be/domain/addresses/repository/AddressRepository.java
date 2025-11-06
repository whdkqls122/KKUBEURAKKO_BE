package com.kkubeurakko.kkubeurakko_be.domain.addresses.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kkubeurakko.kkubeurakko_be.domain.addresses.entity.Addresses;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;

public interface AddressRepository extends JpaRepository<Addresses, Long> {
	@Query("select a from Addresses a where a.user = :user order by a.isPrimary desc")
	List<Addresses> findAllByUser(@Param("user") Users user);
}
