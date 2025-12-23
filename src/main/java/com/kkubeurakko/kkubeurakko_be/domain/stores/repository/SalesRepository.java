package com.kkubeurakko.kkubeurakko_be.domain.stores.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Sales;

public interface SalesRepository extends JpaRepository<Sales, Long> {

	List<Sales> findByDateBetween(LocalDate start, LocalDate end);
}