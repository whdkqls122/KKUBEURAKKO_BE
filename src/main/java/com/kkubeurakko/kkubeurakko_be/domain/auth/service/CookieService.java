package com.kkubeurakko.kkubeurakko_be.domain.auth.service;

import java.time.Duration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface CookieService {
	void addCookie(HttpServletResponse response, String name, String value, Duration maxAge, boolean httpOnly);

	void removeCookie(HttpServletResponse response, String name, boolean httpOnly);

	String getCookieValue(HttpServletRequest request, String name);
}
