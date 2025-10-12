package com.kkubeurakko.kkubeurakko_be.global.oauth.dto;

import java.util.Map;

public class NaverResponse implements OAuth2Response{
	private final Map<String, Object> response;

	@SuppressWarnings("unchecked")
	public NaverResponse(Map<String, Object> attribute) {
		this.response = (Map<String, Object>) attribute.get("response");
	}

	@Override
	public String getProvider() {
		return "naver";
	}

	@Override
	public String getProviderId() {
		return response.get("id").toString();
	}
}
