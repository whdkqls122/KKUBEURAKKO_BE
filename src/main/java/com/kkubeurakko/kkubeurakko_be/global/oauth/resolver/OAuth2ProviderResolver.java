package com.kkubeurakko.kkubeurakko_be.global.oauth.resolver;

import java.util.Map;
import java.util.function.Function;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Component;

import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.GoogleResponse;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.KakaoResponse;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.NaverResponse;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.OAuth2Response;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2ProviderResolver {
	private final Map<String, Function<Map<String, Object>, OAuth2Response>> providerMap =
		Map.of(
			"kakao", KakaoResponse::new,
			"naver", NaverResponse::new,
			"google", GoogleResponse::new
		);

	public OAuth2Response resolve(String providerId, Map<String, Object> attributes) {
		Function<Map<String, Object>, OAuth2Response> creator = providerMap.get(providerId);

		if (creator == null) {
			throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다.");
		}

		return creator.apply(attributes);
	}
}
