package com.kkubeurakko.kkubeurakko_be.global.oauth.dto.token;

import lombok.Builder;

@Builder
public record OAuthTokenResponse(
	String accessToken,
	String refreshToken,
	long expiresInSeconds
) {
}

