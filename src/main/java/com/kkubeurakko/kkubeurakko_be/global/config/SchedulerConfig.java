package com.kkubeurakko.kkubeurakko_be.global.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 스케줄러 활성화. 통합 테스트에서는 scheduling.enabled=false로 꺼서
 * 주기 작업(가게 상태 변경, 실패 작업 재처리 등)이 테스트 데이터를 바꾸지 않게 함
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class SchedulerConfig {
}