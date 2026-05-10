package com.kkubeurakko.kkubeurakko_be.domain.auth.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CookieServiceImpl implements CookieService {
	@Value("${spring.security.cookie.domain}")
	private String domain;

	@Override
	public void addCookie(HttpServletResponse response, String name, String value, Duration maxAge, boolean httpOnly) {
		ResponseCookie cookie = ResponseCookie.from(name, value)
			.maxAge(maxAge)
			.secure(true)
			.httpOnly(httpOnly)
			.sameSite("None")
			.path("/")
			.domain(domain)
			.build();

		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

	@Override
	public void removeCookie(HttpServletResponse response, String name, boolean httpOnly) {
		ResponseCookie cookie = ResponseCookie.from(name, "")
			.maxAge(0)
			.secure(true)
			.httpOnly(httpOnly)
			.sameSite("None")
			.path("/")
			.domain(domain)
			.build();

		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

	@Override
	public String getCookieValue(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return null;
		}

		for (Cookie cookie : cookies) {
			if (name.equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}
}
