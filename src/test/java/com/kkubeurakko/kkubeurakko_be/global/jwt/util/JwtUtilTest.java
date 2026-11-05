package com.kkubeurakko.kkubeurakko_be.global.jwt.util;

import static org.assertj.core.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.kkubeurakko.kkubeurakko_be.domain.users.entity.UserRole;

class JwtUtilTest {

	// HS256은 256비트(32바이트) 이상의 키가 필요
	private static final String SECRET = Base64.getEncoder()
		.encodeToString("test-secret-key-for-jwt-util-0123456789".getBytes(StandardCharsets.UTF_8));
	private static final String OTHER_SECRET = Base64.getEncoder()
		.encodeToString("another-secret-key-for-jwt-util-987654".getBytes(StandardCharsets.UTF_8));
	private static final long EXPIRY = 60_000L;

	private final JwtUtil jwtUtil = new JwtUtil(SECRET);

	@Test
	@DisplayName("임시 토큰은 temp category와 TEMP 권한으로 발급된다")
	void createTempToken() {
		String token = jwtUtil.createTempToken("local_123", EXPIRY);

		assertThat(jwtUtil.validateToken(token)).isTrue();
		assertThat(jwtUtil.getCategory(token)).isEqualTo(JwtUtil.TEMP_CATEGORY);
		assertThat(jwtUtil.getRole(token)).isEqualTo(UserRole.TEMP.toString());
		assertThat(jwtUtil.getUserNumber(token)).isEqualTo("local_123");
	}

	@Test
	@DisplayName("access·refresh 토큰은 각각의 category로 발급된다")
	void createAccessAndRefreshToken() {
		String access = jwtUtil.createAccessToken("123", "CUSTOMER", EXPIRY);
		String refresh = jwtUtil.createRefreshToken("123", "CUSTOMER", EXPIRY);

		assertThat(jwtUtil.getCategory(access)).isEqualTo(JwtUtil.ACCESS_CATEGORY);
		assertThat(jwtUtil.getCategory(refresh)).isEqualTo(JwtUtil.REFRESH_CATEGORY);
	}

	@Test
	@DisplayName("만료된 토큰은 validateToken이 false지만, claims는 여전히 읽힌다")
	void expiredToken() {
		String token = jwtUtil.createTempToken("local_123", -1_000L);

		assertThat(jwtUtil.validateToken(token)).isFalse();
		// getClaims가 만료 토큰의 claims도 반환하므로, 사용 전에 반드시 validateToken으로 검증해야 함
		assertThat(jwtUtil.getUserNumber(token)).isEqualTo("local_123");
	}

	@Test
	@DisplayName("다른 키로 서명된 토큰은 validateToken이 false다")
	void tokenSignedWithOtherKey() {
		String token = new JwtUtil(OTHER_SECRET).createTempToken("local_123", EXPIRY);

		assertThat(jwtUtil.validateToken(token)).isFalse();
	}
}