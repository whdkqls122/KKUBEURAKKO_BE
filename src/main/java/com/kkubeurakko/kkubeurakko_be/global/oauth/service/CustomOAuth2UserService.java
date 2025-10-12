package com.kkubeurakko.kkubeurakko_be.global.oauth.service;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.kkubeurakko.kkubeurakko_be.domain.users.dto.UserDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.UserRole;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.repository.UserRepository;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.OAuth2Response;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final UserRepository userRepository;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = super.loadUser(userRequest);

		log.info("OAuth2User attributes: {}", oAuth2User.getAttributes());

		String userNumber = getUserNumber(userRequest, oAuth2User);

		Users user = userRepository.findByUserNumber(userNumber).orElse(null);
		if (user == null) {
			user = Users.builder()
				.userNumber(userNumber)
				.role(UserRole.CUSTOMER)
				.build();
			userRepository.save(user);
		}

		UserDto userDto = UserDto.builder()
			.userNumber(user.getUserNumber())
			.role(user.getRole())
			.build();

		return new CustomOAuth2User(userDto);
	}

	private String getUserNumber(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		OAuth2Response oAuth2Response = null;

		// if (registrationId.equals("kakao")) {
		// 	oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
		// }
		// 추후 naver, google 추가 가능
		// else if (registrationId.equals("naver")) { ... }
		// else if (registrationId.equals("google")) { ... }

		if (oAuth2Response == null) {
			throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다.");
		}

		return oAuth2Response.getProvider() + "_" + oAuth2Response.getProviderId();
	}
}
