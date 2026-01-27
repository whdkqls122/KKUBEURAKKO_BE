package com.kkubeurakko.kkubeurakko_be.global.oauth;

import static org.mockito.Mockito.*;

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
			tokenService
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

		when(authorizedClient.getAccessToken())
			.thenReturn(mock(org.springframework.security.oauth2.core.OAuth2AccessToken.class));

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

		when(authorizedClient.getAccessToken())
			.thenReturn(mock(org.springframework.security.oauth2.core.OAuth2AccessToken.class));

		successHandler.onAuthenticationSuccess(request, response, authentication);

		verify(tokenService).createRefreshToken(eq(response), eq(TEST_USER));
		verify(response).sendRedirect(contains("/home"));
	}

	@Test
	@DisplayName("로그아웃 수행 후 RefreshToken 삭제 및 블랙리스트 등록을 검증")
	void testLogout() throws Exception {
		// Access / Refresh 토큰 생성
		String accessToken = jwtUtil.createAccessToken(TEST_USER, UserRole.CUSTOMER.getRole(), 1000 * 60 * 10);
		String refreshToken = jwtUtil.createRefreshToken(TEST_USER, UserRole.CUSTOMER.getRole(), 1000 * 60 * 60 * 24);

		// 요청 쿠키 모킹
		Cookie cookie = new Cookie("Authorization", refreshToken);
		when(request.getCookies()).thenReturn(new Cookie[] {cookie});
		when(request.getHeader("Authorization")).thenReturn("Bearer " + accessToken);
		when(request.getRequestURI()).thenReturn("/logout");
		when(request.getMethod()).thenReturn("POST");
		when(refreshTokenRepository.existsByRefresh(refreshToken)).thenReturn(true);

		logoutFilter.doFilter(request, response, filterChain);
		CustomOAuth2User principal = new CustomOAuth2User(
			UserDto.builder()
				.userNumber(TEST_USER)
				.role(UserRole.CUSTOMER)
				.build()
		);
		// RefreshToken 삭제 확인
		verify(refreshTokenRepository, times(1)).deleteById(principal.getName());
		// Redis 블랙리스트 등록 확인
		verify(valueOps, times(1)).set(startsWith("blacklist:"), eq("logout"), anyLong(), eq(TimeUnit.MILLISECONDS));
	}
}
