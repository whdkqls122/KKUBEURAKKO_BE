package com.kkubeurakko.kkubeurakko_be.domain.auth.service;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.service.UserService;
import com.kkubeurakko.kkubeurakko_be.global.jwt.repository.RefreshTokenRepository;
import com.kkubeurakko.kkubeurakko_be.global.jwt.util.JwtUtil;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

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
	private UserService userService;

	@Mock
	private TokenService tokenService;

	@Mock
	private CookieService cookieService;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Test
	@DisplayName("refreshToken 없으면 쿠키만 제거하고 종료되는지 검증한다.")
	void logout_noRefreshToken() {
		// given
		when(tokenService.getRefreshToken(request)).thenReturn(null);

		// when
		logoutService.logout(request, response);

		// then
		verify(tokenService).getRefreshToken(request);
		verify(tokenService).removeRefreshToken(response);
		verify(tokenService, never()).blacklistAccessToken(anyString());
		verify(jwtUtil, never()).getUserNumber(anyString());
		verify(refreshTokenRepository, never()).deleteById(anyString());
		verifyNoInteractions(cookieService);
	}

	@Test
	@DisplayName("access + refreshToken 존재 시 정상 로그아웃 처리가 되는지 검증한다.")
	void logout_success() {
		// given
		when(tokenService.getRefreshToken(request)).thenReturn("refreshToken");
		when(request.getHeader("Authorization")).thenReturn("Bearer accessToken");
		when(jwtUtil.getUserNumber("refreshToken")).thenReturn("user123");

		// when
		logoutService.logout(request, response);

		// then
		verify(tokenService).getRefreshToken(request);
		verify(tokenService).blacklistAccessToken("accessToken");
		verify(jwtUtil).getUserNumber("refreshToken");
		verify(refreshTokenRepository).deleteById("user123");
		verify(tokenService).removeRefreshToken(response);
		verifyNoInteractions(cookieService);
	}

	@Test
	@DisplayName("refreshToken 파싱 실패 시 refresh 쿠키는 제거된다.")
	void logout_refreshTokenParsingFail() {
		// given
		when(tokenService.getRefreshToken(request)).thenReturn("invalidRefreshToken");
		when(jwtUtil.getUserNumber("invalidRefreshToken"))
			.thenThrow(new RuntimeException("invalid token"));

		// when
		logoutService.logout(request, response);

		// then
		verify(jwtUtil).getUserNumber("invalidRefreshToken");
		verify(refreshTokenRepository, never()).deleteById(anyString());
		verify(tokenService).removeRefreshToken(response);
		verifyNoInteractions(cookieService);
	}

	@Test
	@DisplayName("강제 로그아웃 시 access / refresh 토큰 제거 및 쿠키 삭제가 정상적으로 되는지 검증한다.")
	void forceLogout_success() {
		// given
		CustomOAuth2User oAuth2User = mock(CustomOAuth2User.class);
		Users user = mock(Users.class);

		when(oAuth2User.getName()).thenReturn("user123");
		when(userService.getUser("user123")).thenReturn(user);
		when(user.getUserNumber()).thenReturn("user123");

		when(request.getHeader("Authorization")).thenReturn("Bearer accessToken");
		when(tokenService.getRefreshToken(request)).thenReturn("refreshToken");
		when(refreshTokenRepository.existsById("user123")).thenReturn(true);

		// when
		logoutService.forceLogout(oAuth2User, request, response);

		// then
		verify(userService).getUser("user123");
		verify(tokenService).getRefreshToken(request);
		verify(tokenService).blacklistAccessToken("accessToken");
		verify(refreshTokenRepository).existsById("user123");
		verify(refreshTokenRepository).deleteById("user123");
		verify(tokenService).removeRefreshToken(response);
		verify(cookieService).removeCookie(response, "SAVED_ID", false);
	}

	@Test
	@DisplayName("강제 로그아웃 시 refreshToken이 없으면 저장된 refresh는 삭제하지 않는다.")
	void forceLogout_withoutRefreshToken() {
		// given
		CustomOAuth2User oAuth2User = mock(CustomOAuth2User.class);
		Users user = mock(Users.class);

		when(oAuth2User.getName()).thenReturn("user123");
		when(userService.getUser("user123")).thenReturn(user);

		when(request.getHeader("Authorization")).thenReturn("Bearer accessToken");
		when(tokenService.getRefreshToken(request)).thenReturn(null);

		// when
		logoutService.forceLogout(oAuth2User, request, response);

		// then
		verify(tokenService).blacklistAccessToken("accessToken");
		verify(refreshTokenRepository, never()).existsById(anyString());
		verify(refreshTokenRepository, never()).deleteById(anyString());
		verify(tokenService).removeRefreshToken(response);
		verify(cookieService).removeCookie(response, "SAVED_ID", false);
	}

	@Test
	@DisplayName("강제 로그아웃 시 Authorization 헤더가 없으면 blacklist 없이 처리된다.")
	void forceLogout_withoutAccessToken() {
		// given
		CustomOAuth2User oAuth2User = mock(CustomOAuth2User.class);
		Users user = mock(Users.class);

		when(oAuth2User.getName()).thenReturn("user123");
		when(userService.getUser("user123")).thenReturn(user);
		when(user.getUserNumber()).thenReturn("user123");

		when(request.getHeader("Authorization")).thenReturn(null);
		when(tokenService.getRefreshToken(request)).thenReturn("refreshToken");
		when(refreshTokenRepository.existsById("user123")).thenReturn(true);

		// when
		logoutService.forceLogout(oAuth2User, request, response);

		// then
		verify(tokenService, never()).blacklistAccessToken(anyString());
		verify(refreshTokenRepository).deleteById("user123");
		verify(tokenService).removeRefreshToken(response);
		verify(cookieService).removeCookie(response, "SAVED_ID", false);
	}

	@Test
	@DisplayName("강제 로그아웃 중 예외가 발생해도 refresh와 SAVED_ID 쿠키 삭제는 수행된다.")
	void forceLogout_exception_butFinallyWorks() {
		// given
		CustomOAuth2User oAuth2User = mock(CustomOAuth2User.class);
		when(oAuth2User.getName()).thenReturn("user123");
		when(userService.getUser("user123")).thenThrow(new RuntimeException("user lookup fail"));

		// when
		logoutService.forceLogout(oAuth2User, request, response);

		// then
		verify(tokenService).removeRefreshToken(response);
		verify(cookieService).removeCookie(response, "SAVED_ID", false);
		verifyNoInteractions(refreshTokenRepository);
	}
}
