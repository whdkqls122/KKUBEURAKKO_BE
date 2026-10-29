package com.kkubeurakko.kkubeurakko_be;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Skipping context load due to DB absence in CI")
class KkubeurakkoBeApplicationTests {

	@Test
	void contextLoads() {
	}

}
