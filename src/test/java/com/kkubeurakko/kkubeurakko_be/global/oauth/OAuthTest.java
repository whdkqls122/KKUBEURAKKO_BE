package com.kkubeurakko.kkubeurakko_be.global.oauth;

import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkubeurakko.kkubeurakko_be.domain.auth.service.LogoutService;
import com.kkubeurakko.kkubeurakko_be.domain.auth.service.TokenService;
import com.kkubeurakko.kkubeurakko_be.domain.users.dto.UserDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.UserRole;
import com.kkubeurakko.kkubeurakko_be.domain.users.repository.UserRepository;
import com.kkubeurakko.kkubeurakko_be.global.jwt.repository.RefreshTokenRepository;
import com.kkubeurakko.kkubeurakko_be.global.jwt.util.JwtUtil;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;
import com.kkubeurakko.kkubeurakko_be.global.oauth.handler.CustomSuccessHandler;
import com.kkubeurakko.kkubeurakko_be.global.oauth.service.OAuth2TokenService;
import com.kkubeurakko.kkubeurakko_be.global.security.filter.CustomLogoutFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class OAuthTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private RefreshTokenRepository refreshTokenRepository;

	@Mock
	private RedisTemplate<String, String> redisTemplate;

	@Mock
	private ValueOperations<String, String> valueOps;

	@Mock
	private TokenService tokenService;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Mock
	private FilterChain filterChain;

	@Mock
	private ObjectMapper objectMapper;

	@Mock
	private OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

	@Mock
	private LogoutService logoutService;
	@Mock
	private OAuth2TokenService oAuth2TokenService;

	private JwtUtil jwtUtil;
	private CustomSuccessHandler successHandler;
	private CustomLogoutFilter logoutFilter;
	private final String TEST_USER = "google_7777";

	@BeforeEach
	void setUp() {
		lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);

		successHandler = new CustomSuccessHandler(
			refreshTokenRepository,
			oAuth2AuthorizedClientService,
			redisTemplate,
			tokenService,
			oAuth2TokenService
		);

		String secret = Base64.getEncoder()
			.encodeToString("01234567890123456789012345678901".getBytes());

		jwtUtil = new JwtUtil(secret);

		logoutFilter = new CustomLogoutFilter(logoutService);
	}

	@Test
	@DisplayName("첫 로그인 시 임시 토큰 발급 및 약관 페이지로 리다이렉트")
	void testFirstLogin() throws Exception {
		// given
		UserDto userDto = UserDto.builder()
			.userNumber(TEST_USER)
			.role(UserRole.CUSTOMER)
			.firstLogin(true)
			.build();

		CustomOAuth2User principal = new CustomOAuth2User(userDto);

		OAuth2AuthenticationToken authentication =
			new OAuth2AuthenticationToken(
				principal,
				principal.getAuthorities(),
				"google"
			);

		OAuth2AuthorizedClient authorizedClient = mock(OAuth2AuthorizedClient.class);
		when(oAuth2AuthorizedClientService.loadAuthorizedClient(any(), any()))
			.thenReturn(authorizedClient);

		OAuth2AccessToken accessToken = new OAuth2AccessToken(
			OAuth2AccessToken.TokenType.BEARER,
			"provider-access-token",
			Instant.now(),
			Instant.now().plusSeconds(3600)
		);
		when(authorizedClient.getAccessToken()).thenReturn(accessToken);

		// when
		successHandler.onAuthenticationSuccess(request, response, authentication);

		// then
		verify(tokenService).createTempToken(eq(response), eq(TEST_USER));
		verify(response).sendRedirect(contains("/verify/agreement"));
	}

	@Test
	@DisplayName("기존 회원 로그인 시 RefreshToken 발급 및 홈으로 이동")
	void testNormalLogin() throws Exception {
		UserDto userDto = UserDto.builder()
			.userNumber(TEST_USER)
			.role(UserRole.CUSTOMER)
			.firstLogin(false)
			.build();

		CustomOAuth2User principal = new CustomOAuth2User(userDto);

		OAuth2AuthenticationToken authentication =
			new OAuth2AuthenticationToken(
				principal,
				principal.getAuthorities(),
				"google"
			);

		OAuth2AuthorizedClient authorizedClient = mock(OAuth2AuthorizedClient.class);
		when(oAuth2AuthorizedClientService.loadAuthorizedClient(any(), any()))
			.thenReturn(authorizedClient);

		OAuth2AccessToken accessToken = new OAuth2AccessToken(
			OAuth2AccessToken.TokenType.BEARER,
			"provider-access-token",
			Instant.now(),
			Instant.now().plusSeconds(3600)
		);
		when(authorizedClient.getAccessToken()).thenReturn(accessToken);

		successHandler.onAuthenticationSuccess(request, response, authentication);

		verify(tokenService).createRefreshToken(eq(response), eq(TEST_USER));
		verify(response).sendRedirect(contains("/home"));
	}

	@Test
	@DisplayName("로그아웃 요청이면 LogoutService를 호출하고 다음 필터로 넘기지 않는다")
	void testLogout() throws Exception {
		when(request.getRequestURI()).thenReturn("/v1/logout");
		when(request.getMethod()).thenReturn("POST");

		logoutFilter.doFilter(request, response, filterChain);

		verify(logoutService).logout(request, response);
		verify(filterChain, never()).doFilter(request, response);
	}
}
