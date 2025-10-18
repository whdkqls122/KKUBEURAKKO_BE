package com.kkubeurakko.kkubeurakko_be.domain.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.LoginRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Admins;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.UserRole;
import com.kkubeurakko.kkubeurakko_be.domain.users.repository.AdminRepository;
import com.kkubeurakko.kkubeurakko_be.global.jwt.entity.RefreshToken;
import com.kkubeurakko.kkubeurakko_be.global.jwt.repository.RefreshTokenRepository;
import com.kkubeurakko.kkubeurakko_be.global.jwt.util.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class LoginServiceImplTest {

	@Mock
	private AdminRepository adminRepository;
	@Mock
	private JwtUtil jwtUtil;
	@Mock
	private RefreshTokenRepository refreshTokenRepository;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private HttpServletResponse response;

	@InjectMocks
	private LoginServiceImpl loginService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		loginService = new LoginServiceImpl(adminRepository, jwtUtil, refreshTokenRepository, passwordEncoder);
		org.springframework.test.util.ReflectionTestUtils.setField(loginService, "accessTokenExpiry", 1000L);
		org.springframework.test.util.ReflectionTestUtils.setField(loginService, "refreshTokenExpiry", 2000L);
	}

	@AfterEach
	void clearContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("정상 로그인 시 SecurityContext, 토큰, 쿠키가 설정되는 것을 검증한다.")
	void adminLoginSuccess() {
		// given
		LoginRequestDto dto = new LoginRequestDto("admin1", "1234");
		Admins admin = Admins.builder()
			.id("admin1")
			.password("encodedPassword")
			.role(UserRole.ADMIN)
			.build();

		when(adminRepository.findById("admin1")).thenReturn(Optional.of(admin));
		when(passwordEncoder.matches("1234", "encodedPassword")).thenReturn(true);
		when(jwtUtil.createAccessToken(any(), any(), anyLong())).thenReturn("accessToken");
		when(jwtUtil.createRefreshToken(any(), any(), anyLong())).thenReturn("refreshToken");

		// when
		String result = loginService.adminLogin(dto, response);

		// then
		assertThat(result).isEqualTo("accessToken");
		assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo("admin1");

		verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
		verify(response, times(1)).addCookie(argThat(cookie ->
			cookie.getName().equals("refresh") &&
				cookie.isHttpOnly() &&
				cookie.getValue().equals("refreshToken")
		));
	}

	@Test
	@DisplayName("비밀번호 불일치 시 예외가 발생하는 것을 검증한다.")
	void adminLoginWrongPassword() {
		// given
		LoginRequestDto dto = new LoginRequestDto("admin1", "wrong");
		Admins admin = Admins.builder()
			.id("admin1")
			.password("encodedPassword")
			.role(UserRole.ADMIN)
			.build();

		when(adminRepository.findById("admin1")).thenReturn(Optional.of(admin));
		when(passwordEncoder.matches("wrong", "encodedPassword")).thenReturn(false);

		// then
		assertThatThrownBy(() -> loginService.adminLogin(dto, response))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("비밀번호가 일치하지 않습니다.");
	}

	@Test
	@DisplayName("존재하지 않는 관리자 로그인 시 NullPointerException 발생")
	void adminLogin_adminNotFound() {
		// given
		when(adminRepository.findById("unknown")).thenReturn(Optional.empty());
		LoginRequestDto dto = new LoginRequestDto("unknown", "pw");

		// then
		assertThatThrownBy(() -> loginService.adminLogin(dto, response))
			.isInstanceOf(NullPointerException.class);
	}
}
