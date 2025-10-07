package com.kkubeurakko.kkubeurakko_be.global.config;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import com.kkubeurakko.kkubeurakko_be.global.exception.GlobalAccessDeniedHandler;
import com.kkubeurakko.kkubeurakko_be.global.exception.GlobalAuthenticationEntryPoint;
import com.kkubeurakko.kkubeurakko_be.global.jwt.JwtFilter;
import com.kkubeurakko.kkubeurakko_be.global.jwt.JwtUtil;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	private final JwtUtil jwtUtil;
	private final GlobalAuthenticationEntryPoint authenticationEntryPoint;
	private final GlobalAccessDeniedHandler accessDeniedHandler;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
			.cors(corsCustomizer -> corsCustomizer.configurationSource(request -> {
				CorsConfiguration configuration = new CorsConfiguration();

				configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
				configuration.setAllowedMethods(Collections.singletonList("*"));
				configuration.setAllowCredentials(true);
				configuration.setAllowedHeaders(Collections.singletonList("*"));
				configuration.setMaxAge(3600L);

				configuration.setExposedHeaders(Collections.singletonList("Set-Cookie"));
				configuration.setExposedHeaders(Collections.singletonList("Authorization"));

				return configuration;
			}))
			.csrf((auth) -> auth.disable())
			.formLogin(auth -> auth.disable())
			.httpBasic((auth) -> auth.disable())
			.addFilterBefore(
				new JwtFilter(jwtUtil),
				UsernamePasswordAuthenticationFilter.class
			)
			// .addFilterBefore(
			// 	new CustomLogoutFilter(jwtUtil, refreshTokenRepository),
			// 	LogoutFilter.class
			// )
			// //JWTFilter 추가
			// .addFilterAfter(new JwtFilter(jwtUtil), OAuth2LoginAuthenticationFilter.class)
			// .oauth2Login(
			// 	((oauth2) -> oauth2
			// 		.userInfoEndpoint((userInfoEndpointConfig ->
			// 			userInfoEndpointConfig.userService(customOAuth2UserService))
			// 		).successHandler(customSuccessHandler)
			// 	)
			// )
			.exceptionHandling(exceptionHandling -> exceptionHandling
				.authenticationEntryPoint(authenticationEntryPoint)
				.accessDeniedHandler(accessDeniedHandler)
			)
			.authorizeHttpRequests((auth) -> auth
				.requestMatchers("/").permitAll()
					.requestMatchers(
							"/swagger-ui/**",
							"/v3/api-docs/**",
							"/swagger-ui.html",
							"/swagger-resources/**",
							"/webjars/**"
					).permitAll()
				.anyRequest().authenticated()
			)
			.sessionManagement((session) -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).build();
	}
}
