package com.kkubeurakko.kkubeurakko_be.domain.users.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kkubeurakko.kkubeurakko_be.TestFixture;
import com.kkubeurakko.kkubeurakko_be.domain.users.dto.UserInfoResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.dto.UserPhoneResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.repository.UserRepository;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;
import com.kkubeurakko.kkubeurakko_be.global.security.util.AesUtil;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
	@Mock
	private AesUtil aesUtil;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserServiceImpl userService;

	@Test
	@DisplayName("유저 정보를 정상적으로 가져오는지 검증한다.")
	void getUserInfo() {
		// given
		String userNumber = "kakao_12345";
		String decryptedPhone = "010-1234-5678";
		CustomOAuth2User customOAuth2User =
			new CustomOAuth2User(
				com.kkubeurakko.kkubeurakko_be.domain.users.dto.UserDto.builder()
					.userNumber(userNumber)
					.build()
			);

		Users user = TestFixture.createUser(userNumber, "암호화된 휴대전화번호");

		when(userRepository.findByUserNumber(userNumber))
			.thenReturn(Optional.of(user));

		when(aesUtil.decryptFromBase64("암호화된 휴대전화번호"))
			.thenReturn(decryptedPhone);
		// when
		UserInfoResponseDto result = userService.getUserInfo(customOAuth2User);

		// then
		assertThat(result.email()).isEqualTo("test@naver.com");
		assertThat(result.nickname()).isEqualTo("조아빈");
		assertThat(result.provider()).isEqualTo("kakao");
		assertThat(result.phone()).isEqualTo("010-12**-56**");
	}

	@Test
	@DisplayName("유저 휴대폰 번호를 정상적으로 가져오는지 검증한다.")
	void getUserPhone() {
		// given
		String userNumber = "kakao_12345";
		String decryptedPhone = "010-1234-5678";
		CustomOAuth2User customOAuth2User =
			new CustomOAuth2User(
				com.kkubeurakko.kkubeurakko_be.domain.users.dto.UserDto.builder()
					.userNumber(userNumber)
					.build()
			);

		Users user = TestFixture.createUser(userNumber, "암호화된 휴대전화번호");

		when(userRepository.findByUserNumber(userNumber))
			.thenReturn(Optional.of(user));

		when(aesUtil.decryptFromBase64("암호화된 휴대전화번호"))
			.thenReturn(decryptedPhone);
		// when
		UserPhoneResponseDto result = userService.getUserPhone(customOAuth2User);

		// then
		assertThat(result.phone()).isEqualTo("010-1234-5678");
	}
}