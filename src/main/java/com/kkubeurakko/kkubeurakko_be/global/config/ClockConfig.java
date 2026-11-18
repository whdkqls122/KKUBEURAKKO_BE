package com.kkubeurakko.kkubeurakko_be.global.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {
	// 생성 시각과 같은 기준이 되도록 서버 기본 시간대를 사용한다
	@Bean
	public Clock clock() {
		return Clock.systemDefaultZone();
	}
}