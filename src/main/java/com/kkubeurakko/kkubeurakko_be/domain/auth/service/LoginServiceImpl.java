package com.kkubeurakko.kkubeurakko_be.domain.auth.service;

import java.time.Duration;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.LoginRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.UserLoginRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Admins;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.repository.AdminRepository;
import com.kkubeurakko.kkubeurakko_be.domain.users.repository.UserRepository;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginServiceImpl implements LoginService {
	private final AdminRepository adminRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;
	private final TokenService tokenService;
	private final CookieService cookieService;

	private static final String SAVED_ID_COOKIE_NAME = "SAVED_ID";

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
		return tokenService.createLoginToken(response, admin.getId(), admin.getRole().toString());
	}

	@Override
	public String login(UserLoginRequestDto loginRequestDto, HttpServletResponse response) {
		Users user = userRepository.findById(loginRequestDto.id()).orElseThrow(NullPointerException::new);

		if (!passwordEncoder.matches(loginRequestDto.password(), user.getPassword())) {
			throw new RuntimeException("비밀번호가 일치하지 않습니다.");
		}

		Authentication authentication =
			new UsernamePasswordAuthenticationToken(
				user.getUserNumber(),
				null,
				List.of(new SimpleGrantedAuthority(user.getRole().toString()))
			);

		SecurityContextHolder.getContext().setAuthentication(authentication);

		createSavedIdCookie(response, loginRequestDto.id(), loginRequestDto.isSaveId());

		return tokenService.createLoginToken(response, user.getUserNumber(), user.getRole().toString());
	}

	private void createSavedIdCookie(HttpServletResponse response, String id, boolean saveId) {
		if (!saveId) {
			cookieService.removeCookie(response, "SAVED_ID", false);
			return;
		}
		cookieService.addCookie(response, SAVED_ID_COOKIE_NAME, id, Duration.ofDays(30), false);
	}
}
