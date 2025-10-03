package com.kkubeurakko.kkubeurakko_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
public class KkubeurakkoBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(KkubeurakkoBeApplication.class, args);
	}

}
