package com.kkubeurakko.kkubeurakko_be.domain.payments.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kkubeurakko.kkubeurakko_be.domain.payments.entity.Payments;

@Repository
public interface PaymentRepository extends JpaRepository<Payments, Long> {
}
