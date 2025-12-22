package com.kkubeurakko.kkubeurakko_be.domain.users.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.PortOneVerifyInfoResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.portone.service.PortOneService;
import com.kkubeurakko.kkubeurakko_be.domain.users.dto.UserVerifyRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.exception.UserVerifyException;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;
import com.kkubeurakko.kkubeurakko_be.global.oauth.exception.OAUthUnlinkException;
import com.kkubeurakko.kkubeurakko_be.global.oauth.service.OAuthUnlinkService;

@ExtendWith(MockitoExtension.class)
class UserVerifyFacadeServiceImplTest {
	@Mock
	private PortOneService portOneService;

	@Mock
	private UserService userService;

	@Mock
	private OAuthUnlinkService oAuthUnlinkService;

	@InjectMocks
	private UserVerifyFacadeServiceImpl facadeService;

	private Users user;
	private CustomOAuth2User customOAuth2User;

	@BeforeEach
	void setUp() {
		user = mock(Users.class);
		customOAuth2User = mock(CustomOAuth2User.class);
		when(customOAuth2User.getName()).thenReturn("kakao_1234");
	}

	@Test
	@DisplayName("사용자 인증 및 전화번호 저장이 정상적으로 이루어지는지 검증한다.")
	void verifyAndSavePhone() {
		// given
		UserVerifyRequestDto requestDto = mock(UserVerifyRequestDto.class);
		PortOneVerifyInfoResponseDto responseDto = mock(PortOneVerifyInfoResponseDto.class);

		when(userService.getUser("kakao_1234")).thenReturn(user);
		when(portOneService.getPortOneVerifyInfo(any()))
			.thenReturn(responseDto);

		// when
		facadeService.verifyAndSavePhone(requestDto, customOAuth2User);

		// then
		verify(userService).saveUserPhone(user, responseDto);
		verify(userService, never()).deleteUser(any());
		verify(oAuthUnlinkService, never()).revokeOAuthAccount(any());
	}

	@Test
	@DisplayName("전화번호 저장 과정 도중 문제 발생 시 의도된 예외가 발생하는지 검증한다.")
	void verifyAndSavePhoneUserVerifyException() {
		// given
		UserVerifyRequestDto requestDto = mock(UserVerifyRequestDto.class);

		when(user.getUserNumber()).thenReturn("kakao_1234");
		when(userService.getUser("kakao_1234")).thenReturn(user);
		when(portOneService.getPortOneVerifyInfo(any()))
			.thenThrow(new RuntimeException("본인인증 실패"));

		// when & then
		assertThrows(UserVerifyException.class,
			() -> facadeService.verifyAndSavePhone(requestDto, customOAuth2User)
		);

		verify(userService).deleteUser(user);
		verify(oAuthUnlinkService).revokeOAuthAccount("kakao_1234");
	}

	@Test
	@DisplayName("사용자 탈퇴 및 oAuthUnlink 요청이 정상적으로 이루어지는지 검증한다.")
	void deleteUserAndUnlink() {
		// given
		when(userService.getUser("kakao_1234")).thenReturn(user);
		when(user.getUserNumber()).thenReturn("kakao_1234");

		// when
		facadeService.deleteUserAndUnlink(customOAuth2User);

		// then
		verify(oAuthUnlinkService).revokeOAuthAccount("kakao_1234");
		verify(userService).deleteUser(user);
	}

	@Test
	@DisplayName("탈퇴 중 문제 발생 시 예외가 발생하는지 검증한다.")
	void deleteUserAndUnlinkThrowsException() {
		// given
		when(userService.getUser("kakao_1234")).thenReturn(user);
		doThrow(new RuntimeException("OAuthUnlink 실패"))
			.when(oAuthUnlinkService).revokeOAuthAccount("kakao_1234");

		// when & then
		assertThrows(OAUthUnlinkException.class,
			() -> facadeService.deleteUserAndUnlink(customOAuth2User)
		);

		verify(userService, never()).deleteUser(any());
	}
}