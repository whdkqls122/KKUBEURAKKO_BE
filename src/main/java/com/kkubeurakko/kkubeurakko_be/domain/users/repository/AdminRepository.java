package com.kkubeurakko.kkubeurakko_be.domain.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Admins;
@Repository
public interface AdminRepository extends JpaRepository<Admins, String> {
}
