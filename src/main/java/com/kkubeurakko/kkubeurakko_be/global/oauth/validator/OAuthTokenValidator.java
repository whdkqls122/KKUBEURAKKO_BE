package com.kkubeurakko.kkubeurakko_be.global.oauth.validator;

import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.OAuthProvider;

public interface OAuthTokenValidator {
	OAuthProvider supports();

	boolean validate(String accessToken);
}
