package com.kkubeurakko.kkubeurakko_be.domain.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface TokenService {
	String reissue(HttpServletRequest request);

	void createRefreshToken(HttpServletResponse response, String userNumber);
}
