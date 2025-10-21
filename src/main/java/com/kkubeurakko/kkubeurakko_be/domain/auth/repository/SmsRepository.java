package com.kkubeurakko.kkubeurakko_be.domain.auth.repository;

import org.springframework.data.repository.CrudRepository;

import com.kkubeurakko.kkubeurakko_be.domain.auth.entity.Sms;

public interface SmsRepository extends CrudRepository<Sms, String> {
}
