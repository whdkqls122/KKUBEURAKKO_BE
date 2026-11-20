package com.kkubeurakko.kkubeurakko_be;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.kkubeurakko.kkubeurakko_be.support.IntegrationTestSupport;

class KkubeurakkoBeApplicationTests extends IntegrationTestSupport {

	@Test
	@DisplayName("MySQL, Redis 컨테이너와 테스트 설정으로 애플리케이션 컨텍스트가 뜬다")
	void contextLoads() {
	}
}