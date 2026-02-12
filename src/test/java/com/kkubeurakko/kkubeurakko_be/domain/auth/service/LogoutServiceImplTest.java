package com.kkubeurakko.kkubeurakko_be.domain.auth.service;

import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.service.UserService;
import com.kkubeurakko.kkubeurakko_be.global.jwt.repository.RefreshTokenRepository;
import com.kkubeurakko.kkubeurakko_be.global.jwt.util.JwtUtil;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class LogoutServiceImplTest {

	@InjectMocks
	private LogoutServiceImpl logoutService;

	@Mock
	private JwtUtil jwtUtil;

	@Mock
	private RefreshTokenRepository refreshTokenRepository;

	@Mock
	private RedisTemplate<String, String> redisTemplate;

	@Mock
	private ValueOperations<String, String> valueOperations;

	@Mock
	private UserService userService;

	@Mock
	private TokenService tokenService;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Test
	@DisplayName("refreshToken 없으면 쿠키만 제거하고 종료되는지 검증한다.")
	void logout_noRefreshToken() {
		// given
		when(request.getCookies()).thenReturn(null);

		// when
		logoutService.logout(request, response);

		// then
		verify(tokenService).removeRefreshToken(response);
		verifyNoInteractions(refreshTokenRepository);
	}

	@Test
	@DisplayName("access + refreshToken 존재 시 정상 로그아웃 처리가 되는지 검증한다.")
	void logout_success() {
		// given
		Cookie refreshCookie = new Cookie("refresh", "refreshToken");
		when(request.getCookies()).thenReturn(new Cookie[] {refreshCookie});
		when(request.getHeader("Authorization"))
			.thenReturn("Bearer accessToken");

		when(jwtUtil.getUserNumber("refreshToken"))
			.thenReturn("user123");

		when(jwtUtil.getRemainingExpiration("accessToken"))
			.thenReturn(1000L);

		when(redisTemplate.opsForValue())
			.thenReturn(valueOperations);

		// when
		logoutService.logout(request, response);

		// then
		verify(valueOperations).set(
			eq("blacklist:accessToken"),
			eq("logout"),
			eq(1000L),
			eq(TimeUnit.MILLISECONDS)
		);

		verify(refreshTokenRepository).deleteById("user123");
		verify(tokenService).removeRefreshToken(response);
	}

	@Test
	@DisplayName("강제 로그아웃 시 access / refresh 토큰 제거 및 쿠키 삭제가 정상적으로 되는지 검증한다.")
	void forceLogout_success() {
		// given
		CustomOAuth2User oAuth2User = mock(CustomOAuth2User.class);
		when(oAuth2User.getName()).thenReturn("user123");

		Users user = mock(Users.class);
		when(user.getUserNumber()).thenReturn("user123");
		when(userService.getUser("user123")).thenReturn(user);

		when(request.getHeader("Authorization"))
			.thenReturn("Bearer accessToken");

		Cookie refreshCookie = new Cookie("refresh", "refreshToken");
		when(request.getCookies()).thenReturn(new Cookie[] {refreshCookie});

		when(jwtUtil.getRemainingExpiration("accessToken"))
			.thenReturn(500L);

		when(redisTemplate.opsForValue())
			.thenReturn(valueOperations);

		when(refreshTokenRepository.existsById("user123"))
			.thenReturn(true);

		// when
		logoutService.forceLogout(oAuth2User, request, response);

		// then
		verify(valueOperations).set(
			eq("blacklist:accessToken"),
			eq("logout"),
			eq(500L),
			eq(TimeUnit.MILLISECONDS)
		);

		verify(refreshTokenRepository).deleteById("user123");

		verify(response, atLeastOnce())
			.addHeader(eq("Set-Cookie"), anyString());

		verify(tokenService).removeRefreshToken(response);
	}
}
