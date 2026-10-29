package com.kkubeurakko.kkubeurakko_be.global.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
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

	@Value("${cors.allowed-origins}")
	private List<String> allowedOrigins;

	private static final String ADMIN = UserRole.ADMIN.name();

	private static final String[] PUBLIC_URLS = {
		"/", "/actuator/health",
		"/v1/admin/login", "/v1/user/login", "/v1/reissue", "/v1/logout",
		"/v1/signup/init", "/v1/signup/duplicate",
		"/v1/user/verify-phone", "/v1/user/complete-signup",
		"/v1/account/id", "/v1/account/id/check", "/v1/account/next-step",
		"/v1/account/password/verification", "/v1/account/password",
		"/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**"
	};

	private static final String[] CUSTOMER_GET_URLS = {
		"/v1/coupons/issued"
	};

	private static final String[] ADMIN_URLS = {
		"/v1/admin/**", "/v1/sales/**",
		"/v1/menu/admin/**", "/v1/menu/detail/admin/*",
		"/v1/menu/option/**", "/v1/menu/option-group/**"
	};
	private static final String[] ADMIN_GET_URLS = {
		"/v1/orders/*/orders", "/v1/coupons/*"
	};
	private static final String[] ADMIN_POST_URLS = {
		"/v1/menu/menu-group", "/v1/store/**", "/v1/delivery/*", "/v1/coupons/*"
	};
	private static final String[] ADMIN_PUT_URLS = {
		"/v1/menu/menu-group"
	};
	private static final String[] ADMIN_PATCH_URLS = {
		"/v1/menu/menu-group/**", "/v1/orders/*/*", "/v1/store/**", "/v1/delivery/*", "/v1/coupons/*/*/*"
	};
	private static final String[] ADMIN_DELETE_URLS = {
		"/v1/menu/menu-group/**", "/v1/store/**"
	};

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
			.cors(corsCustomizer -> corsCustomizer.configurationSource(request -> {
				CorsConfiguration configuration = new CorsConfiguration();

				configuration.setAllowedOrigins(allowedOrigins);
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
				.requestMatchers(PUBLIC_URLS).permitAll()

				.requestMatchers(HttpMethod.GET, CUSTOMER_GET_URLS).authenticated()

				.requestMatchers(ADMIN_URLS).hasAuthority(ADMIN)
				.requestMatchers(HttpMethod.GET, ADMIN_GET_URLS).hasAuthority(ADMIN)
				.requestMatchers(HttpMethod.POST, ADMIN_POST_URLS).hasAuthority(ADMIN)
				.requestMatchers(HttpMethod.PUT, ADMIN_PUT_URLS).hasAuthority(ADMIN)
				.requestMatchers(HttpMethod.PATCH, ADMIN_PATCH_URLS).hasAuthority(ADMIN)
				.requestMatchers(HttpMethod.DELETE, ADMIN_DELETE_URLS).hasAuthority(ADMIN)

				.requestMatchers(HttpMethod.GET, "/v1/menu/**").permitAll()

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