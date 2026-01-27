package com.kkubeurakko.kkubeurakko_be.domain.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;

import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.service.UserService;
import com.kkubeurakko.kkubeurakko_be.global.jwt.entity.RefreshToken;
import com.kkubeurakko.kkubeurakko_be.global.jwt.repository.RefreshTokenRepository;
import com.kkubeurakko.kkubeurakko_be.global.jwt.util.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class TokenServiceImplTest {
	@Mock
	private JwtUtil jwtUtil;

	@Mock
	private RefreshTokenRepository refreshTokenRepository;

	@Mock
	private UserService userService;

	@Mock
	private HttpServletRequest request;

	@InjectMocks
	private TokenServiceImpl reissueService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		// @Value로 주입되는 값 직접 설정
		reissueService = new TokenServiceImpl(jwtUtil, refreshTokenRepository, userService);
		org.springframework.test.util.ReflectionTestUtils.setField(reissueService, "accessTokenExpiry", 3600000L);
	}

	@Test
	@DisplayName("정상 재발급 성공 검증")
	void reissueTest() {
		// given
		String refreshToken = "refreshToken";
		Cookie cookie = new Cookie("refresh", refreshToken);
		when(request.getCookies()).thenReturn(new Cookie[] {cookie});

		when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
		when(jwtUtil.getUserNumber(refreshToken)).thenReturn("123");
		when(jwtUtil.getRole(refreshToken)).thenReturn("ROLE_USER");
		when(refreshTokenRepository.findByUserNumber("123"))
			.thenReturn(new RefreshToken("123", refreshToken));

		when(jwtUtil.createAccessToken("123", "ROLE_USER", 3600000L))
			.thenReturn("accessToken");

		// when
		String result = reissueService.reissue(request);

		// then
		assertEquals("accessToken", result);
	}

	@Test
	@DisplayName("쿠키가 아예 없을 때 예외가 제대로 발생하는지 검증한다.")
	void testExceptionNotFoundRefreshCookie() {
		when(request.getCookies()).thenReturn(null);

		assertThrows(AuthenticationCredentialsNotFoundException.class, () ->
			reissueService.reissue(request)
		);
	}

	@Test
	@DisplayName("refresh 이름의 쿠키가 없을 때 예외가 제대로 발생하는지 검증한다.")
	void testExceptionNotFoundCookie() {
		Cookie[] cookies = {new Cookie("other", "token")};
		when(request.getCookies()).thenReturn(cookies);

		assertThrows(BadCredentialsException.class, () ->
			reissueService.reissue(request)
		);
	}

	@Test
	@DisplayName("요청 refreshToken과 redis에 저장된 refreshToken과 불일치 시 예외가 제대로 발생하는지 검증한다.")
	void testNotValidRefreshToken() {
		String refreshToken = "refresh_token";
		Cookie cookie = new Cookie("refresh", refreshToken);
		when(request.getCookies()).thenReturn(new Cookie[] {cookie});

		when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
		when(jwtUtil.getUserNumber(refreshToken)).thenReturn("123");
		when(refreshTokenRepository.findByUserNumber("123"))
			.thenReturn(new RefreshToken("123", "different_refresh_token"));

		assertThrows(RuntimeException.class, () ->
			reissueService.reissue(request)
		);
	}

	@Test
	@DisplayName("temp 쿠키가 존재하면 tempToken을 정상 반환한다")
	void getTempToken_success() {
		// given
		Cookie tempCookie = new Cookie("temp", "tempToken");
		when(request.getCookies()).thenReturn(new Cookie[] {tempCookie});

		// when
		String result = reissueService.getTempToken(request);

		// then
		assertEquals("tempToken", result);
	}

	@Test
	@DisplayName("temp 쿠키가 없으면 BadCredentialsException 발생")
	void getTempToken_notFound() {
		// given
		Cookie otherCookie = new Cookie("other", "token");
		when(request.getCookies()).thenReturn(new Cookie[] {otherCookie});

		// then
		assertThrows(BadCredentialsException.class, () ->
			reissueService.getTempToken(request)
		);
	}

	@Test
	@DisplayName("쿠키가 null이면 AuthenticationCredentialsNotFoundException 발생")
	void getTempToken_cookieNull() {
		when(request.getCookies()).thenReturn(null);

		assertThrows(AuthenticationCredentialsNotFoundException.class, () ->
			reissueService.getTempToken(request)
		);
	}

	@Test
	@DisplayName("temp 토큰으로 access / refresh 토큰 생성 성공")
	void createToken_success() {
		// given
		HttpServletResponse response = mock(HttpServletResponse.class);

		Cookie tempCookie = new Cookie("temp", "tempToken");
		when(request.getCookies()).thenReturn(new Cookie[] {tempCookie});

		when(jwtUtil.getUserNumber("tempToken")).thenReturn("123");

		Users user = mock(Users.class);
		when(user.getUserNumber()).thenReturn("123");
		when(user.getRole()).thenReturn(com.kkubeurakko.kkubeurakko_be.domain.users.entity.UserRole.CUSTOMER);

		when(userService.getUser("123")).thenReturn(user);

		when(jwtUtil.createAccessToken(any(), any(), anyLong()))
			.thenReturn("accessToken");
		when(jwtUtil.createRefreshToken(any(), any(), anyLong()))
			.thenReturn("refreshToken");

		// when
		String access = reissueService.createToken(request, response);

		// then
		assertEquals("accessToken", access);

		verify(refreshTokenRepository).save(any(RefreshToken.class));
		verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
	}
}