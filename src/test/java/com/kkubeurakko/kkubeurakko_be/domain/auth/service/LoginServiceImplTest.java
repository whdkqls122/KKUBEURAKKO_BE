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
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.LoginRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.UserLoginRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Admins;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.UserRole;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.repository.AdminRepository;
import com.kkubeurakko.kkubeurakko_be.domain.users.repository.UserRepository;
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
	private UserRepository userRepository;
	@Mock
	private HttpServletResponse response;

	@InjectMocks
	private LoginServiceImpl loginService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		loginService = new LoginServiceImpl(adminRepository, jwtUtil, refreshTokenRepository, passwordEncoder,
			userRepository);
		org.springframework.test.util.ReflectionTestUtils.setField(loginService, "accessTokenExpiry", 1000L);
		org.springframework.test.util.ReflectionTestUtils.setField(loginService, "refreshTokenExpiry", 2000L);
	}

	@AfterEach
	void clearContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("정상 관리자 로그인 시 SecurityContext, 토큰, refresh 쿠키가 설정된다.")
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
		assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
			.isEqualTo("admin1");

		verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));

		verify(response, times(1)).addHeader(
			eq(HttpHeaders.SET_COOKIE),
			argThat(cookie ->
				cookie.contains("refresh=refreshToken") &&
					cookie.contains("SameSite=None") &&
					cookie.contains("Secure")
			)
		);
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

	@Test
	@DisplayName("유저 로그인 + 아이디 저장 시 refresh, SAVED_ID 쿠키가 설정된다.")
	void userLoginWithSaveId() {
		// given
		UserLoginRequestDto dto = UserLoginRequestDto.builder()
			.id("user1")
			.password("1234")
			.isSaveId(true)
			.build();

		Users user = Users.builder()
			.userNumber("user1")
			.password("encodedPassword")
			.role(UserRole.CUSTOMER)
			.build();

		when(userRepository.findById("user1")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("1234", "encodedPassword")).thenReturn(true);
		when(jwtUtil.createAccessToken(any(), any(), anyLong())).thenReturn("accessToken");
		when(jwtUtil.createRefreshToken(any(), any(), anyLong())).thenReturn("refreshToken");

		// when
		String result = loginService.login(dto, response);

		// then
		assertThat(result).isEqualTo("accessToken");

		verify(refreshTokenRepository).save(any(RefreshToken.class));

		verify(response, times(2)).addHeader(
			eq(HttpHeaders.SET_COOKIE),
			anyString()
		);

		verify(response).addHeader(
			eq(HttpHeaders.SET_COOKIE),
			argThat(cookie -> cookie.contains("SAVED_ID=user1"))
		);
	}

	@Test
	@DisplayName("유저 로그인 + 아이디 저장 해제 시 SAVED_ID 쿠키가 삭제된다.")
	void userLoginWithoutSaveId() {
		// given
		UserLoginRequestDto dto = UserLoginRequestDto.builder()
			.id("user1")
			.password("1234")
			.isSaveId(false)
			.build();

		Users user = Users.builder()
			.userNumber("user1")
			.password("encodedPassword")
			.role(UserRole.CUSTOMER)
			.build();

		when(userRepository.findById("user1")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("1234", "encodedPassword")).thenReturn(true);
		when(jwtUtil.createAccessToken(any(), any(), anyLong())).thenReturn("accessToken");
		when(jwtUtil.createRefreshToken(any(), any(), anyLong())).thenReturn("refreshToken");

		// when
		loginService.login(dto, response);

		// then
		verify(response).addHeader(
			eq(HttpHeaders.SET_COOKIE),
			argThat(cookie ->
				cookie.contains("SAVED_ID=") &&
					cookie.contains("Max-Age=0")
			)
		);
	}

}
