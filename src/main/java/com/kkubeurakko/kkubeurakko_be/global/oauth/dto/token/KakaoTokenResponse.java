package com.kkubeurakko.kkubeurakko_be.global.oauth.dto.token;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoTokenResponse(
	@JsonProperty("access_token") String accessToken,
	@JsonProperty("refresh_token") String refreshToken,
	@JsonProperty("expires_in") long expiresIn
) {
	public OAuthTokenResponse toOAuthTokenResponse() {
		return OAuthTokenResponse
			.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.expiresInSeconds(expiresIn)
			.build();
	}
}
