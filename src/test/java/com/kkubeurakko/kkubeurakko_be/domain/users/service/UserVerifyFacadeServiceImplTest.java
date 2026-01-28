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
import org.springframework.data.redis.core.RedisTemplate;

import com.kkubeurakko.kkubeurakko_be.domain.auth.service.LogoutService;
import com.kkubeurakko.kkubeurakko_be.domain.auth.service.TokenService;
import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.PortOneVerifyInfoResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.portone.service.PortOneService;
import com.kkubeurakko.kkubeurakko_be.domain.users.dto.TempUserDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.dto.UserVerifyRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.exception.UserVerifyException;
import com.kkubeurakko.kkubeurakko_be.global.jwt.util.JwtUtil;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;
import com.kkubeurakko.kkubeurakko_be.global.oauth.exception.OAUthUnlinkException;
import com.kkubeurakko.kkubeurakko_be.global.oauth.service.OAuthUnlinkService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class UserVerifyFacadeServiceImplTest {
	@Mock
	private PortOneService portOneService;

	@Mock
	private UserService userService;

	@Mock
	private OAuthUnlinkService oAuthUnlinkService;

	@Mock
	private TokenService tokenService;

	@Mock
	private JwtUtil jwtUtil;

	@Mock
	private RedisTemplate<String, Object> redisTemplate;
	@Mock
	private LogoutService logoutService;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Mock
	private org.springframework.data.redis.core.ValueOperations<String, Object> valueOperations;

	@InjectMocks
	private UserVerifyFacadeServiceImpl facadeService;

	private static final String TEMP_USER_KEY = "TEMP_USER:123";

	private Users user;
	private CustomOAuth2User customOAuth2User;

	@BeforeEach
	void setUp() {
		user = mock(Users.class);
		customOAuth2User = mock(CustomOAuth2User.class);
	}

	@Test
	@DisplayName("임시 사용자 본인인증 후 회원 생성 성공")
	void verifyAndSaveUser_success() {
		// given
		UserVerifyRequestDto requestDto = mock(UserVerifyRequestDto.class);
		PortOneVerifyInfoResponseDto portOneDto = mock(PortOneVerifyInfoResponseDto.class);

		TempUserDto tempUserDto = TempUserDto.builder()
			.userNumber("123")
			.build();

		when(tokenService.getTempToken(request)).thenReturn("tempToken");
		when(jwtUtil.getUserNumber("tempToken")).thenReturn("123");

		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get(TEMP_USER_KEY)).thenReturn(tempUserDto);

		when(portOneService.getPortOneVerifyInfo(any()))
			.thenReturn(portOneDto);

		// when
		facadeService.verifyAndSaveUser(requestDto, request, response);

		// then
		verify(userService).createUser(tempUserDto, portOneDto);
		verify(redisTemplate).delete(TEMP_USER_KEY);
		verify(oAuthUnlinkService, never()).revokeOAuthAccount(any());
		verify(tokenService, never()).removeTempToken(any());
	}

	@Test
	@DisplayName("본인인증 실패 시 tempToken 제거 + OAuth unlink 수행")
	void verifyAndSaveUser_fail() {
		// given
		UserVerifyRequestDto requestDto = mock(UserVerifyRequestDto.class);

		TempUserDto tempUserDto = TempUserDto.builder()
			.userNumber("123")
			.build();

		when(tokenService.getTempToken(request)).thenReturn("tempToken");
		when(jwtUtil.getUserNumber("tempToken")).thenReturn("123");

		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get(TEMP_USER_KEY)).thenReturn(tempUserDto);

		when(portOneService.getPortOneVerifyInfo(any()))
			.thenThrow(new RuntimeException("본인인증 실패"));

		// when & then
		assertThrows(UserVerifyException.class, () ->
			facadeService.verifyAndSaveUser(requestDto, request, response)
		);

		verify(tokenService).removeTempToken(response);
		verify(oAuthUnlinkService).revokeOAuthAccount("123");
		verify(redisTemplate).delete(TEMP_USER_KEY);
	}

	@Test
	@DisplayName("사용자 탈퇴 및 oAuthUnlink 요청이 정상적으로 이루어지는지 검증한다.")
	void deleteUserAndUnlink() {
		// given
		when(customOAuth2User.getName()).thenReturn("kakao_1234");
		when(userService.getUser("kakao_1234")).thenReturn(user);
		when(user.getUserNumber()).thenReturn("kakao_1234");
		// when
		facadeService.deleteUserAndUnlink(customOAuth2User, request, response);

		// then
		verify(oAuthUnlinkService).revokeOAuthAccount("kakao_1234");
		verify(logoutService).forceLogout(customOAuth2User, request, response);
		verify(userService).deleteUser(user);
	}

	@Test
	@DisplayName("OAuth unlink 실패 시 OAUthUnlinkException 발생")
	void deleteUserAndUnlink_fail() {
		// given
		CustomOAuth2User principal = mock(CustomOAuth2User.class);
		when(principal.getName()).thenReturn("123");

		// when & then
		assertThrows(OAUthUnlinkException.class, () ->
			facadeService.deleteUserAndUnlink(principal, request, response)
		);

		verify(redisTemplate, never()).delete((String)any());
	}
}