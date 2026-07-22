package com.kkubeurakko.kkubeurakko_be.global.security.filter;

import java.io.IOException;

import org.springframework.web.filter.GenericFilterBean;

import com.kkubeurakko.kkubeurakko_be.domain.auth.service.LogoutService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class CustomLogoutFilter extends GenericFilterBean {

	private final LogoutService logoutService;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws
		IOException,
		ServletException {
		if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
			doFilter((HttpServletRequest)request, (HttpServletResponse)response, chain);
		} else {
			chain.doFilter(request, response);
		}
	}

	private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws
		IOException,
		ServletException {
		if (!isLogoutRequest(request)) {
			chain.doFilter(request, response);
			return;
		}
		try {
			logoutService.logout(request, response);

			response.setStatus(HttpServletResponse.SC_OK);
			log.info("로그아웃 성공");

		} catch (Exception e) {
			log.error("로그아웃 실패", e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private boolean isLogoutRequest(HttpServletRequest request) {
		return "/v1/logout".equals(request.getRequestURI()) && "POST".equalsIgnoreCase(request.getMethod());
	}
}
