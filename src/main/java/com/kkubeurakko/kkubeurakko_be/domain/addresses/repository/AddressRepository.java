package com.kkubeurakko.kkubeurakko_be.domain.addresses.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kkubeurakko.kkubeurakko_be.domain.addresses.entity.Addresses;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;

public interface AddressRepository extends JpaRepository<Addresses, Long> {
	List<Addresses> findAllByUser(Users user);
}
