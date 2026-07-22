package com.kkubeurakko.kkubeurakko_be;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
class KkubeurakkoBeApplicationTests {

	@Test
	@Disabled("Skipping context load due to DB absence in CI")
	void contextLoads() {
	}

}
