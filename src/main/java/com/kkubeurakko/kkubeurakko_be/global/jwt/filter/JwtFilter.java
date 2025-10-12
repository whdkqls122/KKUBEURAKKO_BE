package com.kkubeurakko.kkubeurakko_be.global.jwt.filter;

import java.io.IOException;

import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.kkubeurakko.kkubeurakko_be.domain.users.dto.UserDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.UserRole;
import com.kkubeurakko.kkubeurakko_be.global.jwt.util.JwtUtil;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
	private final JwtUtil jwtUtil;

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		String accessToken = request.getHeader("Authorization");

		//헤더가 없는 경우
		if (accessToken == null || !accessToken.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}
		//Bearer 제거
		accessToken = accessToken.substring(7);

		try {
			if (jwtUtil.isExpired(accessToken)) {
				throw new InsufficientAuthenticationException("Jwt가 만료되었습니다.");
			}
			String category = jwtUtil.getCategory(accessToken);
			if (!"access".equals(category)) {
				throw new IllegalArgumentException("잘못된 토큰 유형");
			}

			String userNumber = jwtUtil.getUserNumber(accessToken);
			String role = jwtUtil.getRole(accessToken);
			UserRole userRole = UserRole.findUserRole(role);

			CustomOAuth2User principal = new CustomOAuth2User(
				UserDto.builder()
					.userNumber(userNumber)
					.role(userRole).
					build()
			);

			Authentication authentication = new UsernamePasswordAuthenticationToken(
				principal, null, principal.getAuthorities()
			);
			SecurityContextHolder.getContext().setAuthentication(authentication);
		} catch (
			ExpiredJwtException e) {
			throw new InsufficientAuthenticationException("JWT가 만료되었습니다.", e);
		} catch (IllegalArgumentException e) {
			throw new InsufficientAuthenticationException("JWT 검증 실패: " + e.getMessage(), e);
		}

		// ✅ 다음 필터로 진행
		filterChain.doFilter(request, response);
	}
}
