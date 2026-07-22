package com.kkubeurakko.kkubeurakko_be.domain.auth.service;

import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.LoginRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.UserLoginRequestDto;

import jakarta.servlet.http.HttpServletResponse;

public interface LoginService {
	//관리자 로그인
	String adminLogin(LoginRequestDto loginRequestDto, HttpServletResponse response);

	//일반 사용자 로그인
	String login(UserLoginRequestDto loginRequestDto, HttpServletResponse response);
}
