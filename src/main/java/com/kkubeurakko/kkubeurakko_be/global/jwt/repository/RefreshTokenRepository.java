package com.kkubeurakko.kkubeurakko_be.global.jwt.repository;

import org.springframework.data.repository.CrudRepository;

import com.kkubeurakko.kkubeurakko_be.global.jwt.entity.RefreshToken;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
	boolean existsByRefresh(String refreshToken);

	void deleteByRefresh(String refreshToken);
}
