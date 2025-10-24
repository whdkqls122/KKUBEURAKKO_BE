package com.kkubeurakko.kkubeurakko_be.domain.auth.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.kkubeurakko.kkubeurakko_be.domain.auth.entity.Sms;

@Repository
public interface SmsRepository extends CrudRepository<Sms, String> {
}
