package com.kkubeurakko.kkubeurakko_be.global.oauth.service;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.kkubeurakko.kkubeurakko_be.domain.users.dto.TempUserDto;
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
	private final RedisTemplate<String, TempUserDto> redisTemplate;

	private static final String TEMP_USER_KEY_PREFIX = "TEMP_USER:";

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = super.loadUser(userRequest);

		log.info("OAuth2User attributes: {}", oAuth2User.getAttributes());

		String provider = userRequest.getClientRegistration().getRegistrationId();
		OAuth2Response response = oAuth2ProviderResolver.resolve(provider, oAuth2User.getAttributes());
		String userNumber = response.getProvider() + "_" + response.getProviderId();

		Users user = userRepository.findByUserNumber(userNumber).orElse(null);
		UserDto userDto;
		boolean firstLogin = (user == null);
		if (firstLogin) {
			TempUserDto tempUserDto = TempUserDto.builder()
				.userNumber(userNumber)
				.email(response.getEmail())
				.nickname(response.getNickname())
				.build();

			redisTemplate.opsForValue().set(
				TEMP_USER_KEY_PREFIX + userNumber,
				tempUserDto,
				Duration.ofMinutes(10)
			);

			userDto = UserDto.builder()
				.userNumber(userNumber)
				.role(UserRole.TEMP)
				.firstLogin(true)
				.build();
		} else {
			user.updateNickname(response.getNickname());
			userDto = UserDto.builder()
				.userNumber(user.getUserNumber())
				.role(user.getRole())
				.firstLogin(false)
				.build();
		}

		log.info("유저 정보 생성 완료:{}", userNumber);
		return new CustomOAuth2User(userDto);
	}
}
