package com.kkubeurakko.kkubeurakko_be.domain.auth.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.LoginRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Admins;
import com.kkubeurakko.kkubeurakko_be.domain.users.repository.AdminRepository;
import com.kkubeurakko.kkubeurakko_be.global.jwt.entity.RefreshToken;
import com.kkubeurakko.kkubeurakko_be.global.jwt.repository.RefreshTokenRepository;
import com.kkubeurakko.kkubeurakko_be.global.jwt.util.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginServiceImpl implements LoginService {
	private final AdminRepository adminRepository;
	private final JwtUtil jwtUtil;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordEncoder passwordEncoder;

	@Value("${spring.jwt.access-token-expiry}")
	private long accessTokenExpiry;

	@Value("${spring.jwt.refresh-token-expiry}")
	private long refreshTokenExpiry;

	@Override
	public String adminLogin(LoginRequestDto loginRequestDto, HttpServletResponse response) {
		Admins admin = adminRepository.findById(
			loginRequestDto.adminId()
		).orElseThrow(NullPointerException::new);

		if (!passwordEncoder.matches(loginRequestDto.password(), admin.getPassword())) {
			throw new RuntimeException("비밀번호가 일치하지 않습니다.");
		}

		// 인증 객체 생성
		Authentication authentication =
			new UsernamePasswordAuthenticationToken(
				admin.getId(),
				null,
				List.of(new SimpleGrantedAuthority(admin.getRole().toString()))
			);

		SecurityContextHolder.getContext().setAuthentication(authentication);
		// JWT 발급
		String accessToken = jwtUtil.createAccessToken(admin.getId(), admin.getRole().toString(), accessTokenExpiry);
		String refreshToken = jwtUtil.createRefreshToken(admin.getId(), admin.getRole().toString(), refreshTokenExpiry);

		saveRefreshToken(admin.getId(), refreshToken);

		ResponseCookie cookie = createCookie(refreshToken);
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

		return accessToken;
	}

	private ResponseCookie createCookie(String value) {
		return ResponseCookie.from("refresh", value)
			.maxAge(refreshTokenExpiry / 1000)
			.secure(true)
			// .httpOnly(true)
			.sameSite("None")
			.path("/")
			// .domain(".kkubeurakko-anyang.shop")
			.build();
	}

	private void saveRefreshToken(String adminId, String refreshToken) {
		RefreshToken refreshTokenEntity = RefreshToken.builder()
			.refresh(refreshToken)
			.userNumber(adminId)
			.build();

		refreshTokenRepository.save(refreshTokenEntity);
	}
}
