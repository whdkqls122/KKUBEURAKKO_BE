package com.kkubeurakko.kkubeurakko_be.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;

import com.kkubeurakko.kkubeurakko_be.domain.orders.util.OrderNumberUtil;
import com.kkubeurakko.kkubeurakko_be.domain.portone.service.PortOneService;
import com.kkubeurakko.kkubeurakko_be.global.task.service.FailedTaskService;

/**
 * 통합 테스트 공통 설정. MySQL, Redis 컨테이너를 JVM당 한 번만 띄워 모든 통합 테스트가 함께 씀
 * 컨테이너는 JVM이 끝나면 Testcontainers(Ryuk)가 정리
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class IntegrationTestSupport {
	// 모든 통합 테스트가 같은 mock 구성을 써야 스프링 컨텍스트 하나를 재사용
	// (구성이 다르면 컨텍스트가 새로 뜨고, create-drop 때문에 같은 DB의 테이블을 다시 만든다)

	// 실제 외부 API(PortOne)를 호출하지 않음
	@MockitoBean
	protected PortOneService portOneService;

	// 주문번호 생성은 가게 영업시간 데이터가 필요해 mock으로 대체
	@MockitoBean
	protected OrderNumberUtil orderNumberUtil;

	// 실제 동작을 유지하면서 특정 테스트에서만 저장 실패를 만들 수 있게 함
	@MockitoSpyBean
	protected FailedTaskService failedTaskService;

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