package com.kkubeurakko.kkubeurakko_be;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class KkubeurakkoBeApplicationTests {

	@Test
	@Disabled("Skipping context load due to DB absence in CI")
	void contextLoads() {
	}

}
