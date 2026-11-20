package com.kkubeurakko.kkubeurakko_be.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;

/**
 * 통합 테스트 공통 설정. MySQL, Redis 컨테이너를 JVM당 한 번만 띄워 모든 통합 테스트가 함께 씀
 * 컨테이너는 JVM이 끝나면 Testcontainers(Ryuk)가 정리
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class IntegrationTestSupport {

	private static final int REDIS_PORT = 6379;

	static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0");
	static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7.2-alpine")
		.withExposedPorts(REDIS_PORT);

	static {
		MYSQL.start();
		REDIS.start();
	}

	/**
	 * RedisConfig가 spring.data.redis.host·port를 @Value로 읽어 연결 팩토리를 직접 만들기 때문에
	 * @ServiceConnection 대신 프로퍼티 값을 컨테이너 주소로 변경
	 */
	@DynamicPropertySource
	static void overrideProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
		registry.add("spring.datasource.username", MYSQL::getUsername);
		registry.add("spring.datasource.password", MYSQL::getPassword);
		registry.add("spring.data.redis.host", REDIS::getHost);
		registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(REDIS_PORT));
	}
}