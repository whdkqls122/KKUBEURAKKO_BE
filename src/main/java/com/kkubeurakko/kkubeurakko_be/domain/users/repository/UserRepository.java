package com.kkubeurakko.kkubeurakko_be.domain.users.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;

public interface UserRepository extends JpaRepository<Users, Long> {
	Optional<Users> findByUserNumber(String userNumber);

	boolean existsByUserNumber(String userNumber);
}
