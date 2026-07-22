package com.kkubeurakko.kkubeurakko_be.global.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {
	//http://localhost:8080/swagger-ui/index.html
	@Bean
	public OpenAPI openAPI() {

		String securitySchemeName = "bearerAuth";

		return new OpenAPI()
			.addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
			.components(new Components()
				.addSecuritySchemes(securitySchemeName,
					new SecurityScheme()
						.name(securitySchemeName)
						.type(SecurityScheme.Type.HTTP)
						.scheme("bearer")
						.bearerFormat("JWT")))
			.info(new Info()
				.title("🐔 꾸브라꼬 API 문서")
				.description(" API 명세서입니다.")
				.version("v1.0.0"))
			.servers(List.of(
				new Server().url("http://localhost:8080").description("Local Server")
				// new Server().url("https://api.kkubeurakko-anyang.shop").description("Production Server")
			));
	}
}