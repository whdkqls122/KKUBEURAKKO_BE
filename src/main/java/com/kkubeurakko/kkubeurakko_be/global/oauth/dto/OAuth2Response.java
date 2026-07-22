package com.kkubeurakko.kkubeurakko_be.global.oauth.dto;

public interface OAuth2Response {
	//정보 제공처
	String getProvider();

	// 사용자 고유 id
	String getProviderId();

	//가입 이메일
	String getEmail();

	//사용자 닉네임
	String getNickname();
}
