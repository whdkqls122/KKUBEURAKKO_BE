package com.kkubeurakko.kkubeurakko_be.global.oauth.dto.token;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleTokenResponse(
	@JsonProperty("access_token") String accessToken,
	@JsonProperty("expires_in") long expiresIn
) {
	public OAuthTokenResponse toOAuthTokenResponse() {
		return OAuthTokenResponse
			.builder()
			.accessToken(accessToken)
			.expiresInSeconds(expiresIn)
			.build();
	}
}
