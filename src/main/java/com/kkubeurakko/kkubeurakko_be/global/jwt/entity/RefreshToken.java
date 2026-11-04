package com.kkubeurakko.kkubeurakko_be.global.jwt.entity;

import java.util.concurrent.TimeUnit;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@RedisHash(value = "refreshToken")
public class RefreshToken {
	@Id
	private String userNumber;
	private String refresh;

	//refresh
	@TimeToLive(unit = TimeUnit.MILLISECONDS)
	private Long expiration;

	@Builder
	public RefreshToken(String userNumber, String refresh, Long expiration) {
		this.userNumber = userNumber;
		this.refresh = refresh;
		this.expiration = expiration;
	}
}