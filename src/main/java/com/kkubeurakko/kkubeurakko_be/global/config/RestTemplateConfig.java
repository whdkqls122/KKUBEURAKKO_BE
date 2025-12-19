package com.kkubeurakko.kkubeurakko_be.global.config;

import java.io.IOException;
import java.time.Duration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public RestTemplate oauthRestTemplate() {
		return new RestTemplateBuilder().connectTimeout(Duration.ofSeconds(3)).readTimeout(Duration.ofSeconds(5))
			.errorHandler(new DefaultResponseErrorHandler() {
				@Override
				public boolean hasError(ClientHttpResponse response) throws IOException {
					HttpStatus status = (HttpStatus)response.getStatusCode();
					return status.is5xxServerError();
				}
			})
			.build();
	}
}
