package com.kkubeurakko.kkubeurakko_be.global.oauth.refresher;

import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.OAuthProvider;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.token.OAuthTokenResponse;

public interface OAuthTokenRefresher {
	OAuthProvider supports();

	OAuthTokenResponse refresh(String refreshToken);
}
