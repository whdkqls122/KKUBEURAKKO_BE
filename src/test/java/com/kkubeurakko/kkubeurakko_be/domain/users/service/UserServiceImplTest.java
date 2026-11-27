package com.kkubeurakko.kkubeurakko_be.domain.users.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kkubeurakko.kkubeurakko_be.TestFixture;
import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.PortOneVerifyInfoResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.VerifiedCustomer;
import com.kkubeurakko.kkubeurakko_be.domain.users.dto.OAuthTempUserDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.dto.TempUser;
import com.kkubeurakko.kkubeurakko_be.domain.users.dto.UserInfoResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.dto.UserPhoneResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.exception.DuplicatePhoneException;
import com.kkubeurakko.kkubeurakko_be.domain.users.repository.UserRepository;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;
import com.kkubeurakko.kkubeurakko_be.global.security.util.AesUtil;
import com.kkubeurakko.kkubeurakko_be.global.security.util.PhoneHashUtil;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
	@Mock
	private AesUtil aesUtil;

	@Mock
	private UserRepository userRepository;

	@Mock
	private PhoneHashUtil phoneHashUtil;

	@InjectMocks
	private UserServiceImpl userService;

	private TempUser oauthTempUser() {
		return OAuthTempUserDto.builder()
			.userNumber("kakao_12345")
			.nickname("닉네임")
			.email("test@test.com")
			.build();
	}

	private PortOneVerifyInfoResponseDto verifiedResponse(String phone) {
		return new PortOneVerifyInfoResponseDto("VERIFIED", "verify-id", new VerifiedCustomer(phone, "홍길동"));
	}

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

	@Test
	@DisplayName("가입 시 인증된 번호의 해시로 중복을 확인하고, 같은 해시와 암호화한 번호로 사용자를 저장한다.")
	void createUser_success() {
		// given
		String phone = "01012345678";
		when(phoneHashUtil.hash(phone)).thenReturn("phone-hash");
		when(userRepository.findByPhoneHash("phone-hash")).thenReturn(Optional.empty());
		when(aesUtil.encryptToBase64(phone)).thenReturn("encrypted-phone");

		// when
		userService.createUser(oauthTempUser(), verifiedResponse(phone));

		// then
		ArgumentCaptor<Users> captor = ArgumentCaptor.forClass(Users.class);
		verify(userRepository).save(captor.capture());
		assertThat(captor.getValue().getPhoneHash()).isEqualTo("phone-hash");
		assertThat(captor.getValue().getPhone()).isEqualTo("encrypted-phone");
	}

	@Test
	@DisplayName("같은 해시로 가입한 사용자가 있으면 DuplicatePhoneException이 발생하고 저장하지 않는다.")
	void createUser_duplicatePhone() {
		// given
		String phone = "01012345678";
		when(phoneHashUtil.hash(phone)).thenReturn("phone-hash");
		when(userRepository.findByPhoneHash("phone-hash"))
			.thenReturn(Optional.of(TestFixture.createUser("kakao_99999", "암호화된 휴대전화번호")));

		// when & then
		assertThatThrownBy(() -> userService.createUser(oauthTempUser(), verifiedResponse(phone)))
			.isInstanceOf(DuplicatePhoneException.class);
		verify(userRepository, never()).save(any());
	}
}