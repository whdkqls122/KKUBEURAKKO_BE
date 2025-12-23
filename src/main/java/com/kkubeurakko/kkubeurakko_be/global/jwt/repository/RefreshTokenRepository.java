package com.kkubeurakko.kkubeurakko_be.global.jwt.repository;

import org.springframework.data.repository.CrudRepository;

import com.kkubeurakko.kkubeurakko_be.global.jwt.entity.RefreshToken;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
	RefreshToken findByUserNumber(String userNumber);

	boolean existsByRefresh(String refreshToken);
}
