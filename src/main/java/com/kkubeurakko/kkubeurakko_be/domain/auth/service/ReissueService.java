package com.kkubeurakko.kkubeurakko_be.domain.auth.service;

import jakarta.servlet.http.HttpServletRequest;

public interface ReissueService {
	String reissue(HttpServletRequest request);
}
