package com.kkubeurakko.kkubeurakko_be.global.jwt.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@RedisHash(value = "refreshToken", timeToLive = 60 * 60 * 24 * 7)
public class RefreshToken {
	@Id
	private String userNumber;
	private String refresh;

	@Builder
	public RefreshToken(String userNumber, String refresh) {
		this.userNumber = userNumber;
		this.refresh = refresh;
	}
}