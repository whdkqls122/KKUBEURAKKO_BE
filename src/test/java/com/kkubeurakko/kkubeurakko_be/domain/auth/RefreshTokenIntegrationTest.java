package com.kkubeurakko.kkubeurakko_be.domain.auth;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import com.kkubeurakko.kkubeurakko_be.domain.auth.service.LogoutService;
import com.kkubeurakko.kkubeurakko_be.domain.auth.service.TokenService;
import com.kkubeurakko.kkubeurakko_be.global.jwt.entity.RefreshToken;
import com.kkubeurakko.kkubeurakko_be.global.jwt.repository.RefreshTokenRepository;
import com.kkubeurakko.kkubeurakko_be.global.jwt.util.JwtUtil;
import com.kkubeurakko.kkubeurakko_be.support.IntegrationTestSupport;

import jakarta.servlet.http.Cookie;

/**
 * RefreshToken과 로그아웃 블랙리스트가 실제 Redis에 만료 시간과 함께 저장·삭제되는지 검증
 * 키는 사용자마다 새로 만들어 테스트끼리 섞이지 않음
 */
class RefreshTokenIntegrationTest extends IntegrationTestSupport {

	private static final String ROLE = "CUSTOMER";
	// 저장~조회 사이에 흐른 시간과 초 단위 절삭을 허용하는 오차
	private static final long TTL_TOLERANCE_MS = 5_000;

	@Autowired
	private TokenService tokenService;
	@Autowired
	private LogoutService logoutService;
	@Autowired
	private RefreshTokenRepository refreshTokenRepository;
	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	@Autowired
	private JwtUtil jwtUtil;

	@Value("${spring.jwt.refresh-token-expiry}")
	private long refreshTokenExpiry;
	@Value("${spring.jwt.access-token-expiry}")
	private long accessTokenExpiry;

	private String newUserNumber() {
		return "it-user-" + UUID.randomUUID().toString().substring(0, 8);
	}

	// @RedisHash("refreshToken") + @Id(userNumber)로 저장되는 키
	private String refreshKeyOf(String userNumber) {
		return "refreshToken:" + userNumber;
	}

	private long ttlMillisOf(String key) {
		return stringRedisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
	}

	private MockHttpServletRequest requestWithRefreshCookie(String refreshToken) {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setCookies(new Cookie("refresh", refreshToken));
		return request;
	}

	@Test
	@DisplayName("로그인하면 RefreshToken이 사용자 번호 키로 저장되고 만료 시간이 refresh-token-expiry로 설정된다")
	void login_savesRefreshTokenWithTtl() {
		// given
		String userNumber = newUserNumber();

		// when
		tokenService.createLoginToken(new MockHttpServletResponse(), userNumber, ROLE);

		// then
		String key = refreshKeyOf(userNumber);
		assertThat(stringRedisTemplate.hasKey(key)).isTrue();
		assertThat(ttlMillisOf(key)).isBetween(refreshTokenExpiry - TTL_TOLERANCE_MS, refreshTokenExpiry);
		String savedRefresh = refreshTokenRepository.findByUserNumber(userNumber).getRefresh();
		assertThat(jwtUtil.getUserNumber(savedRefresh)).isEqualTo(userNumber);
	}

	@Test
	@DisplayName("저장된 RefreshToken으로 재발급하면 같은 사용자의 Access Token이 발급된다")
	void reissue_withSavedRefreshToken() {
		// given
		String userNumber = newUserNumber();
		tokenService.createLoginToken(new MockHttpServletResponse(), userNumber, ROLE);
		String savedRefresh = refreshTokenRepository.findByUserNumber(userNumber).getRefresh();

		// when
		String accessToken = tokenService.reissue(requestWithRefreshCookie(savedRefresh));

		// then
		assertThat(jwtUtil.getUserNumber(accessToken)).isEqualTo(userNumber);
		assertThat(jwtUtil.getCategory(accessToken)).isEqualTo("access");
	}

	@Test
	@DisplayName("Redis의 RefreshToken이 만료되면 JWT 자체는 유효해도 재발급이 거부된다")
	void reissue_rejectedAfterRedisExpiry() {
		// given: JWT는 14일 유효하지만 Redis에는 1초만 보관
		String userNumber = newUserNumber();
		String refresh = jwtUtil.createRefreshToken(userNumber, ROLE, refreshTokenExpiry);
		refreshTokenRepository.save(RefreshToken.builder()
			.userNumber(userNumber)
			.refresh(refresh)
			.expiration(1_000L)
			.build());

		// when: Redis가 키를 만료시킬 때까지 대기
		String key = refreshKeyOf(userNumber);
		await().atMost(Duration.ofSeconds(5))
			.until(() -> !Boolean.TRUE.equals(stringRedisTemplate.hasKey(key)));

		// then
		assertThatThrownBy(() -> tokenService.reissue(requestWithRefreshCookie(refresh)))
			.isInstanceOf(AccessDeniedException.class)
			.hasMessage("저장된 refreshToken과 일치하지 않습니다.");
	}

	@Test
	@DisplayName("로그아웃하면 RefreshToken이 삭제되고 Access Token이 남은 유효 시간만큼 블랙리스트에 등록된다")
	void logout_deletesRefreshTokenAndBlacklistsAccessToken() {
		// given
		String userNumber = newUserNumber();
		String accessToken = tokenService.createLoginToken(new MockHttpServletResponse(), userNumber, ROLE);
		String savedRefresh = refreshTokenRepository.findByUserNumber(userNumber).getRefresh();

		MockHttpServletRequest request = requestWithRefreshCookie(savedRefresh);
		request.addHeader("Authorization", "Bearer " + accessToken);

		// when
		logoutService.logout(request, new MockHttpServletResponse());

		// then
		assertThat(stringRedisTemplate.hasKey(refreshKeyOf(userNumber))).isFalse();

		String blacklistKey = "blacklist:" + accessToken;
		assertThat(stringRedisTemplate.hasKey(blacklistKey)).isTrue();
		assertThat(ttlMillisOf(blacklistKey)).isBetween(1L, accessTokenExpiry);
	}
}