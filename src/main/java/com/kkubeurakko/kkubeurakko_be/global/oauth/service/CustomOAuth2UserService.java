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
import com.kkubeurakko.kkubeurakko_be.global.oauth.resolver.OAuth2ProviderResolver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final UserRepository userRepository;
	private final OAuth2ProviderResolver oAuth2ProviderResolver;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = super.loadUser(userRequest);

		log.info("OAuth2User attributes: {}", oAuth2User.getAttributes());

		String provider = userRequest.getClientRegistration().getRegistrationId();
		OAuth2Response response = oAuth2ProviderResolver.resolve(provider, oAuth2User.getAttributes());
		String userNumber = response.getProvider() + "_" + response.getProviderId();

		Users user = userRepository.findByUserNumber(userNumber).orElse(null);
		boolean firstLogin = (user == null);
		if (firstLogin) {
			user = Users.builder()
				.userNumber(userNumber)
				.email(response.getEmail())
				.nickname(response.getNickname())
				.role(UserRole.CUSTOMER)
				.build();
			userRepository.save(user);
		} else {
			user.updateNickname(response.getNickname());
		}

		UserDto userDto = UserDto.builder()
			.userNumber(user.getUserNumber())
			.role(user.getRole())
			.firstLogin(firstLogin)
			.build();

		log.info("유저 정보 생성 완료:{}", userNumber);
		return new CustomOAuth2User(userDto);
	}
}
