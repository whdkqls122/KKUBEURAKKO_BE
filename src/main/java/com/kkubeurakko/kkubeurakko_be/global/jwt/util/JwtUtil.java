package com.kkubeurakko.kkubeurakko_be.global.jwt.util;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtUtil {
	private final SecretKey secretKey;

	public JwtUtil(@Value("${spring.jwt.secret}") String secret) {
		byte[] keyBytes;
		try {
			keyBytes = Decoders.BASE64.decode(secret);
		} catch (IllegalArgumentException e) {
			keyBytes = secret.getBytes(StandardCharsets.UTF_8);
		}
		this.secretKey = Keys.hmacShaKeyFor(keyBytes);
	}

	public String createAccessToken(String userNumber, String role, long expiredMs) {
		return createToken("access", userNumber, role, expiredMs);
	}

	public String createRefreshToken(String userNumber, String role, long expiredMs) {
		return createToken("refresh", userNumber, role, expiredMs);
	}

	private String createToken(String category, String userNumber, String role, long expiredMs) {
		long now = System.currentTimeMillis();
		return Jwts.builder()
			.claim("category", category)
			.claim("userNumber", userNumber)
			.claim("role", role)
			.setIssuedAt(new Date(now))
			.setExpiration(new Date(now + expiredMs))
			.signWith(secretKey, SignatureAlgorithm.HS256)
			.compact();
	}

	//토큰 유효성 검증
	//예외 반환 로직 구현 이후 수정 예정
	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder()
				.setSigningKey(secretKey)
				.build()
				.parseClaimsJws(token);
			return true;
		} catch (SecurityException | MalformedJwtException e) {
			log.error("Jwt 서명 또는 잘못된 형식: {}", e.getMessage());
		} catch (ExpiredJwtException e) {
			log.error("Jwt 만료: {}", e.getMessage());
		} catch (UnsupportedJwtException e) {
			log.error("지원하지 않는 Jwt: {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			log.error("Jwt 값이 비어있거나 잘못됨: {}", e.getMessage());
		}
		return false;
	}

	//토큰 만료 여부
	public boolean isExpired(String token) {
		try {
			Date exp = getClaims(token).getExpiration();
			return exp.before(new Date());
		} catch (ExpiredJwtException e) {
			return true;
		}
	}

	private Claims getClaims(String token) {
		try {
			return Jwts.parserBuilder()
				.setSigningKey(secretKey)
				.build()
				.parseClaimsJws(token)
				.getBody();
		} catch (ExpiredJwtException e) {
			return e.getClaims();
		}
	}

	//각 정보 추출
	public String getUserNumber(String token) {
		return getClaims(token).get("userNumber", String.class);
	}


	public String getRole(String token) {
		return getClaims(token).get("role", String.class);
	}

	public String getCategory(String token) {
		return getClaims(token).get("category", String.class);
	}
}
