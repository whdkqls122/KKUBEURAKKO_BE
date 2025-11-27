package com.kkubeurakko.kkubeurakko_be.domain.users.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kkubeurakko.kkubeurakko_be.domain.users.dto.UserInfoResponseDto;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

	@Mock
	private CustomOAuth2User customOAuth2User;

	@InjectMocks
	private UserServiceImpl userService;

	public UserServiceImplTest() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("유저 정보를 정상적으로 가져오는지 검증한다.")
	void getUserInfo() {
		// given
		when(customOAuth2User.getProvider()).thenReturn("naver");
		when(customOAuth2User.getEmail()).thenReturn("test@naver.com");
		when(customOAuth2User.getNickname()).thenReturn("아빈");

		// when
		UserInfoResponseDto response = userService.getUserInfo(customOAuth2User);

		// then
		assertThat(response.provider()).isEqualTo("naver");
		assertThat(response.email()).isEqualTo("test@naver.com");
		assertThat(response.nickname()).isEqualTo("아빈");
	}
}