package com.kkubeurakko.kkubeurakko_be.domain.auth.service;

import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.LoginRequestDto;

import jakarta.servlet.http.HttpServletResponse;

public interface LoginService {
	String adminLogin(LoginRequestDto loginRequestDto, HttpServletResponse response);
}
