package com.kkubeurakko.kkubeurakko_be.domain.auth.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@RedisHash(value = "sms", timeToLive = 60 * 3)
public class Sms {
	@Id
	private String phoneNum;
	private String certificationCode;

	@Builder
	public Sms(String phoneNum, String certificationCode) {
		this.phoneNum = phoneNum;
		this.certificationCode = certificationCode;
	}
}
