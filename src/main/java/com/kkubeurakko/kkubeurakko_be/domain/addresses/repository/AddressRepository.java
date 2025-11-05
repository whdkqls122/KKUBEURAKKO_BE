package com.kkubeurakko.kkubeurakko_be.domain.addresses.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kkubeurakko.kkubeurakko_be.domain.addresses.entity.Addresses;

public interface AddressRepository extends JpaRepository<Addresses, Long> {
}
