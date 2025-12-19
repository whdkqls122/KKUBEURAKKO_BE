package com.kkubeurakko.kkubeurakko_be.global.oauth.client;

import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.OAuthProvider;

public interface OAuthUnlinkClient {

	OAuthProvider supports();

	void unlink(String accessToken);
}
