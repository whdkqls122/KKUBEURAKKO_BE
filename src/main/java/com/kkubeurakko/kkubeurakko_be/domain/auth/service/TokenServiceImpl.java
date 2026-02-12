package com.kkubeurakko.kkubeurakko_be.domain.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.kkubeurakko.kkubeurakko_be.domain.users.entity.UserRole;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.service.UserService;
import com.kkubeurakko.kkubeurakko_be.global.jwt.entity.RefreshToken;
import com.kkubeurakko.kkubeurakko_be.global.jwt.repository.RefreshTokenRepository;
import com.kkubeurakko.kkubeurakko_be.global.jwt.util.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenServiceImpl implements TokenService {
	private final JwtUtil jwtUtil;
	private final RefreshTokenRepository refreshTokenRepository;
	private final UserService userService;

	@Value("${spring.jwt.access-token-expiry}")
	private long accessTokenExpiry;
	@Value("${spring.jwt.refresh-token-expiry}")
	private long refreshTokenExpiry;
	@Value("${spring.jwt.temp-token-expiry}")
	private long tempTokenExpiry;

	@Override
	public String reissue(HttpServletRequest request) {
		log.info("Accesstoken 재발급 시작");
		String refreshToken = getTokenFromCookies(request, "refresh");
		if (refreshToken == null || refreshToken.isBlank()) {
			throw new AccessDeniedException("Refresh Token 없음");
		}
		//토큰 유효성 검사
		jwtUtil.validateToken(refreshToken);

		//정보 추출 및 토큰 일치 검사
		String userNumber = jwtUtil.getUserNumber(refreshToken);
		isExistRefreshToken(refreshToken, userNumber);
		String role = jwtUtil.getRole(refreshToken);

		log.info("Accesstoken 재발급 성공");
		//토큰 생성 및 반환
		return jwtUtil.createAccessToken(userNumber, role, accessTokenExpiry);
	}

	@Override
	public String createToken(
		HttpServletRequest request,
		HttpServletResponse response
	) {
		String tempToken = getTempToken(request);
		removeCookie(response, "temp");

		String userNumber = jwtUtil.getUserNumber(tempToken);
		Users user = userService.getUser(userNumber);

		String access = jwtUtil.createAccessToken(
			user.getUserNumber(),
			user.getRole().toString(),
			accessTokenExpiry
		);
		String refresh = jwtUtil.createRefreshToken(
			user.getUserNumber(),
			user.getRole().toString(),
			refreshTokenExpiry
		);
		saveRefreshToken(userNumber, refresh);
		response.addHeader(HttpHeaders.SET_COOKIE, createCookie(refresh).toString());
		return access;
	}

	@Override
	public void createRefreshToken(HttpServletResponse response, String userNumber) {
		String refresh = jwtUtil.createRefreshToken(userNumber, UserRole.CUSTOMER.toString(), refreshTokenExpiry);

		saveRefreshToken(userNumber, refresh);
		response.addHeader(HttpHeaders.SET_COOKIE, createCookie(refresh).toString());
	}

	@Override
	public String getTempToken(HttpServletRequest request) {
		return getTokenFromCookies(request, "temp");
	}

	@Override
	public void removeTempToken(HttpServletResponse response) {
		removeCookie(response, "temp");
	}

	@Override
	public void removeRefreshToken(HttpServletResponse response) {
		removeCookie(response, "refresh");
	}

	@Override
	public void createTempToken(HttpServletResponse response, String userNumber) {
		String tempToken = jwtUtil.createAccessToken(userNumber, UserRole.TEMP.toString(), tempTokenExpiry);

		response.addHeader(HttpHeaders.SET_COOKIE, createTempCookie(tempToken).toString());
	}

	private String getTokenFromCookies(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			throw new AccessDeniedException("쿠키가 없습니다.");
		}
		for (Cookie cookie : cookies) {
			if (name.equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		throw new AccessDeniedException(name + " Token이 유효하지 않습니다.");
	}

	private void isExistRefreshToken(String refreshToken, String userNumber) {
		RefreshToken findRefreshToken = refreshTokenRepository.findByUserNumber(userNumber);
		if (findRefreshToken == null || !refreshToken.equals(findRefreshToken.getRefresh())) {
			log.error("저장된 refreshToken과 불일치");
			throw new AccessDeniedException(
				"저장된 refreshToken과 일치하지 않습니다."
			);  // 로그아웃 요청을 보내도록 -> 재로그인 요청하도록(공통 예외처리 구현 이후 수정 예정)
		}
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

	private ResponseCookie createTempCookie(String value) {
		return ResponseCookie.from("temp", value)
			.maxAge(tempTokenExpiry / 1000)
			.secure(true)
			// .httpOnly(true)
			.sameSite("None")
			.path("/")
			// .domain(".kkubeurakko-anyang.shop")
			.build();
	}

	//redis에 저장
	private void saveRefreshToken(String userNumber, String refresh) {
		RefreshToken refreshToken = RefreshToken.builder()
			.userNumber(userNumber)
			.refresh(refresh)
			.build();

		refreshTokenRepository.save(refreshToken);
	}

	private void removeCookie(HttpServletResponse response, String name) {
		ResponseCookie responseCookie = ResponseCookie.from(name, null)
			.maxAge(0)
			.secure(true)
			// .httpOnly(true)
			.sameSite("None")
			.path("/")
			// .domain(".kkubeurakko-anyang.shop")
			.build();

		response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
	}
}
