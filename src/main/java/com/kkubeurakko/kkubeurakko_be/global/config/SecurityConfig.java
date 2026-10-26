package com.kkubeurakko.kkubeurakko_be.global.config;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;

import com.kkubeurakko.kkubeurakko_be.domain.auth.service.LogoutService;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.UserRole;
import com.kkubeurakko.kkubeurakko_be.global.exception.GlobalAccessDeniedHandler;
import com.kkubeurakko.kkubeurakko_be.global.exception.GlobalAuthenticationEntryPoint;
import com.kkubeurakko.kkubeurakko_be.global.jwt.filter.JwtFilter;
import com.kkubeurakko.kkubeurakko_be.global.jwt.util.JwtUtil;
import com.kkubeurakko.kkubeurakko_be.global.oauth.handler.CustomSuccessHandler;
import com.kkubeurakko.kkubeurakko_be.global.oauth.service.CustomOAuth2UserService;
import com.kkubeurakko.kkubeurakko_be.global.security.filter.CustomLogoutFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	private final JwtUtil jwtUtil;
	private final GlobalAuthenticationEntryPoint authenticationEntryPoint;
	private final GlobalAccessDeniedHandler accessDeniedHandler;
	private final CustomOAuth2UserService customOAuth2UserService;
	private final CustomSuccessHandler customSuccessHandler;
	private final RedisTemplate<String, String> redisTemplate;
	private final LogoutService logoutService;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
			.cors(corsCustomizer -> corsCustomizer.configurationSource(request -> {
				CorsConfiguration configuration = new CorsConfiguration();

				configuration.setAllowedOrigins(Arrays.asList(
					"http://localhost:5173",
					"http://localhost:5174",
					"https://www.kkubeurakko-anyang.shop",
					"https://admin.kkubeurakko-anyang.shop"
				));
				configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
				configuration.setAllowCredentials(true);
				configuration.setAllowedHeaders(Collections.singletonList("*"));
				configuration.setMaxAge(3600L);

				configuration.setExposedHeaders(Arrays.asList("Set-Cookie", "Authorization"));
				return configuration;
			}))
			.csrf((auth) -> auth.disable())
			.formLogin(auth -> auth.disable())
			.httpBasic((auth) -> auth.disable())
			.addFilterBefore(
				new JwtFilter(jwtUtil, redisTemplate),
				UsernamePasswordAuthenticationFilter.class
			)
			.addFilterBefore(
				new CustomLogoutFilter(logoutService),
				LogoutFilter.class
			)
			.oauth2Login(
				((oauth2) -> oauth2
					.userInfoEndpoint((userInfoEndpointConfig ->
						userInfoEndpointConfig.userService(customOAuth2UserService))
					).successHandler(customSuccessHandler)
				)
			)
			.exceptionHandling(exceptionHandling -> exceptionHandling
				.authenticationEntryPoint(authenticationEntryPoint)
				.accessDeniedHandler(accessDeniedHandler)
			)
			.authorizeHttpRequests((auth) -> auth
				.requestMatchers("/ws/**").permitAll()
				.requestMatchers(
					"/",
					"/swagger-ui/**",
					"/v3/api-docs/**",
					"/swagger-ui.html",
					"/swagger-resources/**",
					"/webjars/**",
					"/v1/admin/login",
					"/v1/menu/**",
					"/v1/reissue",
					"/actuator/health",
					"/v1/user/verify-phone",
					"/v1/user/complete-signup",
					"/v1/signup/duplicate",
					"/v1/signup/init",
					"/v1/user/login",
					"/v1/account/**",
					"/v1/logout"
				).permitAll()
				.anyRequest().authenticated()
			)
			.sessionManagement((session) -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).build();
	}

	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
