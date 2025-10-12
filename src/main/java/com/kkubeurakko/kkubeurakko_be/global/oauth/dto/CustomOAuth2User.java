package com.kkubeurakko.kkubeurakko_be.global.oauth.dto;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.kkubeurakko.kkubeurakko_be.domain.users.dto.UserDto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {
	private final UserDto userDto;
	@Override
	public Map<String, Object> getAttributes() {
		return Map.of(
			"userNumber", userDto.getUserNumber(),
			"role", userDto.getRole().toString()
		);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of((GrantedAuthority)() -> userDto.getRole().toString());
	}

	@Override
	public String getName() {
		return userDto.getUserNumber();
	}
}
