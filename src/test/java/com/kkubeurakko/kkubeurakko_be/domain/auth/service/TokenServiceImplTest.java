package com.kkubeurakko.kkubeurakko_be.domain.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.access.AccessDeniedException;

import com.kkubeurakko.kkubeurakko_be.domain.users.entity.UserRole;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.service.UserService;
import com.kkubeurakko.kkubeurakko_be.global.jwt.entity.RefreshToken;
import com.kkubeurakko.kkubeurakko_be.global.jwt.repository.RefreshTokenRepository;
import com.kkubeurakko.kkubeurakko_be.global.jwt.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class TokenServiceImplTest {
	private static final String REFRESH_COOKIE_NAME = "refresh";
	private static final String TEMP_COOKIE_NAME = "temp";
	private static final long REFRESH_EXPIRY = 604800000L;

	@Mock
	private JwtUtil jwtUtil;

	@Mock
	private RefreshTokenRepository refreshTokenRepository;

	@Mock
	private UserService userService;

	@Mock
	private CookieService cookieService;

	@Mock
	private RedisTemplate<String, String> redisTemplate;

	@Mock
	private ValueOperations<String, String> valueOperations;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@InjectMocks
	private TokenServiceImpl tokenService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		// @Value로 주입되는 값 직접 설정
		tokenService = new TokenServiceImpl(jwtUtil, refreshTokenRepository, userService, cookieService,
			redisTemplate);
		org.springframework.test.util.ReflectionTestUtils.setField(tokenService, "accessTokenExpiry", 3600000L);
		org.springframework.test.util.ReflectionTestUtils.setField(tokenService, "refreshTokenExpiry", REFRESH_EXPIRY);
		org.springframework.test.util.ReflectionTestUtils.setField(tokenService, "tempTokenExpiry", 600000L);
	}

	// 서명, 만료가 유효하고 category가 일치하는 토큰으로 가정
	private void givenValidToken(String token, String category) {
		when(jwtUtil.validateToken(token)).thenReturn(true);
		when(jwtUtil.getCategory(token)).thenReturn(category);
	}

	@Test
	@DisplayName("정상 재발급 성공 검증")
	void reissueTest() {
		when(cookieService.getCookieValue(request, REFRESH_COOKIE_NAME))
			.thenReturn("refreshToken");
		givenValidToken("refreshToken", JwtUtil.REFRESH_CATEGORY);
		when(jwtUtil.getUserNumber("refreshToken"))
			.thenReturn("123");
		when(jwtUtil.getRole("refreshToken"))
			.thenReturn("ROLE_USER");
		when(refreshTokenRepository.findByUserNumber("123"))
			.thenReturn(new RefreshToken("123", "refreshToken", REFRESH_EXPIRY));
		when(jwtUtil.createAccessToken("123", "ROLE_USER", 3600000L))
			.thenReturn("accessToken");

		String result = tokenService.reissue(request);

		assertEquals("accessToken", result);
		verify(jwtUtil).validateToken("refreshToken");
		verify(refreshTokenRepository).findByUserNumber("123");
	}

	@Test
	@DisplayName("refresh 쿠키가 없으면 재발급 시 예외가 발생한다")
	void reissue_fail_whenRefreshCookieMissing() {
		when(cookieService.getCookieValue(request, REFRESH_COOKIE_NAME))
			.thenReturn(null);

		assertThrows(AccessDeniedException.class, () -> tokenService.reissue(request));

		verify(jwtUtil, never()).validateToken(anyString());
		verify(refreshTokenRepository, never()).findByUserNumber(anyString());
	}

	@Test
	@DisplayName("refresh 쿠키가 blank면 재발급 시 예외가 발생한다")
	void reissue_fail_whenRefreshCookieBlank() {
		when(cookieService.getCookieValue(request, REFRESH_COOKIE_NAME))
			.thenReturn(" ");

		assertThrows(AccessDeniedException.class, () -> tokenService.reissue(request));

		verify(jwtUtil, never()).validateToken(anyString());
		verify(refreshTokenRepository, never()).findByUserNumber(anyString());
	}

	@Test
	@DisplayName("만료, 위조된 refresh 토큰이면 재발급 시 예외가 발생한다")
	void reissue_fail_whenRefreshTokenInvalid() {
		when(cookieService.getCookieValue(request, REFRESH_COOKIE_NAME))
			.thenReturn("expiredToken");
		when(jwtUtil.validateToken("expiredToken")).thenReturn(false);

		assertThrows(AccessDeniedException.class, () -> tokenService.reissue(request));

		verify(refreshTokenRepository, never()).findByUserNumber(anyString());
		verify(jwtUtil, never()).createAccessToken(anyString(), anyString(), anyLong());
	}

	@Test
	@DisplayName("refresh가 아닌 토큰으로 재발급하면 예외가 발생한다")
	void reissue_fail_whenNotRefreshCategory() {
		when(cookieService.getCookieValue(request, REFRESH_COOKIE_NAME))
			.thenReturn("accessToken");
		givenValidToken("accessToken", JwtUtil.ACCESS_CATEGORY);

		assertThrows(AccessDeniedException.class, () -> tokenService.reissue(request));

		verify(refreshTokenRepository, never()).findByUserNumber(anyString());
	}

	@Test
	@DisplayName("요청 refreshToken과 저장된 refreshToken이 다르면 예외가 발생한다")
	void reissue_fail_whenRefreshTokenMismatch() {
		when(cookieService.getCookieValue(request, REFRESH_COOKIE_NAME))
			.thenReturn("refreshToken");
		givenValidToken("refreshToken", JwtUtil.REFRESH_CATEGORY);
		when(jwtUtil.getUserNumber("refreshToken"))
			.thenReturn("123");
		when(refreshTokenRepository.findByUserNumber("123"))
			.thenReturn(new RefreshToken("123", "different_refresh_token", REFRESH_EXPIRY));

		assertThrows(AccessDeniedException.class, () -> tokenService.reissue(request));

		verify(refreshTokenRepository).findByUserNumber("123");
	}

	@Test
	@DisplayName("temp 쿠키가 존재하면 tempToken을 정상 반환한다")
	void getTempToken_success() {
		when(cookieService.getCookieValue(request, TEMP_COOKIE_NAME))
			.thenReturn("tempToken");

		String result = tokenService.getTempToken(request);

		assertEquals("tempToken", result);
		verify(cookieService).getCookieValue(request, TEMP_COOKIE_NAME);
	}

	@Test
	@DisplayName("temp 쿠키가 없으면 null을 반환한다")
	void getTempToken_notFound() {
		when(cookieService.getCookieValue(request, TEMP_COOKIE_NAME))
			.thenReturn(null);

		String result = tokenService.getTempToken(request);

		assertNull(result);
		verify(cookieService).getCookieValue(request, TEMP_COOKIE_NAME);
	}

	@Test
	@DisplayName("유효한 임시 토큰이면 사용자 번호를 반환한다")
	void getTempUserNumber_success() {
		when(cookieService.getCookieValue(request, TEMP_COOKIE_NAME))
			.thenReturn("tempToken");
		givenValidToken("tempToken", JwtUtil.TEMP_CATEGORY);
		when(jwtUtil.getUserNumber("tempToken")).thenReturn("123");

		assertEquals("123", tokenService.getTempUserNumber(request));
	}

	@Test
	@DisplayName("임시 토큰 쿠키가 없으면 예외가 발생한다")
	void getTempUserNumber_fail_whenMissing() {
		when(cookieService.getCookieValue(request, TEMP_COOKIE_NAME))
			.thenReturn(null);

		assertThrows(AccessDeniedException.class, () -> tokenService.getTempUserNumber(request));

		verify(jwtUtil, never()).validateToken(anyString());
	}

	@Test
	@DisplayName("만료·위조된 임시 토큰이면 예외가 발생한다")
	void getTempUserNumber_fail_whenInvalid() {
		when(cookieService.getCookieValue(request, TEMP_COOKIE_NAME))
			.thenReturn("expiredTempToken");
		when(jwtUtil.validateToken("expiredTempToken")).thenReturn(false);

		assertThrows(AccessDeniedException.class, () -> tokenService.getTempUserNumber(request));

		verify(jwtUtil, never()).getUserNumber(anyString());
	}

	@Test
	@DisplayName("임시 토큰 쿠키에 temp가 아닌 토큰이 있으면 예외가 발생한다")
	void getTempUserNumber_fail_whenNotTempCategory() {
		when(cookieService.getCookieValue(request, TEMP_COOKIE_NAME))
			.thenReturn("accessToken");
		givenValidToken("accessToken", JwtUtil.ACCESS_CATEGORY);

		assertThrows(AccessDeniedException.class, () -> tokenService.getTempUserNumber(request));

		verify(jwtUtil, never()).getUserNumber(anyString());
	}

	@Test
	@DisplayName("회원가입 완료 후 temp 토큰으로 access/refresh 토큰을 생성한다")
	void createTokenAfterSignup_success() {
		when(cookieService.getCookieValue(request, TEMP_COOKIE_NAME))
			.thenReturn("tempToken");
		givenValidToken("tempToken", JwtUtil.TEMP_CATEGORY);
		when(jwtUtil.getUserNumber("tempToken"))
			.thenReturn("123");

		Users user = mock(Users.class);
		when(user.getUserNumber()).thenReturn("123");
		when(user.getRole()).thenReturn(UserRole.CUSTOMER);
		when(userService.getUser("123")).thenReturn(user);

		when(jwtUtil.createAccessToken("123", UserRole.CUSTOMER.toString(), 3600000L))
			.thenReturn("accessToken");
		when(jwtUtil.createRefreshToken("123", UserRole.CUSTOMER.toString(), REFRESH_EXPIRY))
			.thenReturn("refreshToken");

		String result = tokenService.createTokenAfterSignup(request, response);

		assertEquals("accessToken", result);

		verify(cookieService).removeCookie(response, TEMP_COOKIE_NAME, true);
		verify(refreshTokenRepository).save(any(RefreshToken.class));
		verify(cookieService).addCookie(
			eq(response),
			eq(REFRESH_COOKIE_NAME),
			eq("refreshToken"),
			eq(Duration.ofMillis(REFRESH_EXPIRY)),
			eq(true)
		);
	}

	@Test
	@DisplayName("임시 토큰이 유효하지 않으면 회원가입 후 토큰을 발급하지 않고 쿠키도 지우지 않는다")
	void createTokenAfterSignup_fail_whenTempTokenInvalid() {
		when(cookieService.getCookieValue(request, TEMP_COOKIE_NAME))
			.thenReturn("expiredTempToken");
		when(jwtUtil.validateToken("expiredTempToken")).thenReturn(false);

		assertThrows(AccessDeniedException.class, () -> tokenService.createTokenAfterSignup(request, response));

		verify(cookieService, never()).removeCookie(any(), anyString(), anyBoolean());
		verify(jwtUtil, never()).createAccessToken(anyString(), anyString(), anyLong());
	}

	@Test
	@DisplayName("temp 토큰 생성 시 temp category로 발급해 temp 쿠키에 저장")
	void createTempToken_success() {
		when(jwtUtil.createTempToken("local_123", 600000L))
			.thenReturn("tempToken");

		tokenService.createTempToken(response, "local_123");

		verify(jwtUtil, never()).createAccessToken(anyString(), anyString(), anyLong());
		verify(cookieService).addCookie(
			eq(response),
			eq(TEMP_COOKIE_NAME),
			eq("tempToken"),
			eq(Duration.ofMillis(600000L)),
			eq(true)
		);
	}

	// ===== 로그인·refresh =====

	@Test
	@DisplayName("로그인 시 access/refresh 토큰 생성 및 refresh 쿠키 저장")
	void createLoginToken_success() {
		when(jwtUtil.createAccessToken("user123", "ROLE_USER", 3600000L))
			.thenReturn("accessToken");
		when(jwtUtil.createRefreshToken("user123", "ROLE_USER", REFRESH_EXPIRY))
			.thenReturn("refreshToken");

		String result = tokenService.createLoginToken(response, "user123", "ROLE_USER");

		assertEquals("accessToken", result);
		verify(refreshTokenRepository).save(any(RefreshToken.class));
		verify(cookieService).addCookie(
			eq(response),
			eq(REFRESH_COOKIE_NAME),
			eq("refreshToken"),
			eq(Duration.ofMillis(REFRESH_EXPIRY)),
			eq(true)
		);
	}

	@Test
	@DisplayName("refresh 토큰 저장 시 expiration을 refresh 토큰 만료 시간으로 설정한다")
	void saveRefreshToken_setsExpirationToRefreshExpiry() {
		when(jwtUtil.createRefreshToken("user123", UserRole.CUSTOMER.toString(), REFRESH_EXPIRY))
			.thenReturn("refreshToken");

		tokenService.createRefreshToken(response, "user123");

		ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
		verify(refreshTokenRepository).save(captor.capture());
		assertEquals(REFRESH_EXPIRY, captor.getValue().getExpiration());
		assertEquals("refreshToken", captor.getValue().getRefresh());
	}

	@Test
	@DisplayName("refresh 토큰 생성 시 CUSTOMER 권한으로 refresh 쿠키 저장")
	void createRefreshToken_success() {
		when(jwtUtil.createRefreshToken("user123", UserRole.CUSTOMER.toString(), REFRESH_EXPIRY))
			.thenReturn("refreshToken");

		tokenService.createRefreshToken(response, "user123");

		verify(refreshTokenRepository).save(any(RefreshToken.class));
		verify(cookieService).addCookie(
			eq(response),
			eq(REFRESH_COOKIE_NAME),
			eq("refreshToken"),
			eq(Duration.ofMillis(REFRESH_EXPIRY)),
			eq(true)
		);
	}

	// ===== 쿠키·블랙리스트 =====

	@Test
	@DisplayName("refresh 쿠키 제거")
	void removeRefreshToken_success() {
		tokenService.removeRefreshToken(response);

		verify(cookieService).removeCookie(response, REFRESH_COOKIE_NAME, true);
	}

	@Test
	@DisplayName("temp 쿠키 제거")
	void removeTempToken_success() {
		tokenService.removeTempToken(response);

		verify(cookieService).removeCookie(response, TEMP_COOKIE_NAME, true);
	}

	@Test
	@DisplayName("refresh 쿠키 조회")
	void getRefreshToken_success() {
		when(cookieService.getCookieValue(request, REFRESH_COOKIE_NAME))
			.thenReturn("refreshToken");

		String result = tokenService.getRefreshToken(request);

		assertEquals("refreshToken", result);
		verify(cookieService).getCookieValue(request, REFRESH_COOKIE_NAME);
	}

	@Test
	@DisplayName("access 토큰 블랙리스트 저장")
	void blacklistAccessToken_success() {
		when(jwtUtil.getRemainingExpiration("accessToken"))
			.thenReturn(1000L);
		when(redisTemplate.opsForValue())
			.thenReturn(valueOperations);

		tokenService.blacklistAccessToken("accessToken");

		verify(valueOperations).set(
			eq("blacklist:accessToken"),
			eq("logout"),
			eq(1000L),
			eq(TimeUnit.MILLISECONDS)
		);
	}

	@Test
	@DisplayName("남은 만료 시간이 0 이하이면 블랙리스트 저장하지 않는다")
	void blacklistAccessToken_skipWhenExpired() {
		when(jwtUtil.getRemainingExpiration("accessToken"))
			.thenReturn(0L);

		tokenService.blacklistAccessToken("accessToken");

		verify(redisTemplate, never()).opsForValue();
	}
}