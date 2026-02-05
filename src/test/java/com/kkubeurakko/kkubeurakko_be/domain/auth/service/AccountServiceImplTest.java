package com.kkubeurakko.kkubeurakko_be.domain.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.ExistUserRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.ResetPasswordMeRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.ResetPasswordRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.UserIdResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.PortOneVerifyInfoResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.VerifiedCustomer;
import com.kkubeurakko.kkubeurakko_be.domain.portone.service.PortOneService;
import com.kkubeurakko.kkubeurakko_be.domain.users.dto.UserVerifyRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.repository.UserRepository;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

	@Mock
	private PortOneService portOneService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private org.springframework.data.redis.core.ValueOperations<String, Object> valueOperations;

	@InjectMocks
	private AccountServiceImpl accountService;

	@Test
	@DisplayName("휴대폰 인증 후 ID 마스킹 반환이 정상적으로 이루어진다.")
	void findUserId_success() {
		// given
		String phone = "01012345678";
		String userId = "testUser";

		UserVerifyRequestDto dto = new UserVerifyRequestDto("verifyId");

		PortOneVerifyInfoResponseDto responseDto = mock(PortOneVerifyInfoResponseDto.class);
		VerifiedCustomer customer = mock(VerifiedCustomer.class);
		Users user = mock(Users.class);

		when(portOneService.getPortOneVerifyInfo("verifyId")).thenReturn(responseDto);
		when(responseDto.verifiedCustomer()).thenReturn(customer);
		when(customer.phoneNumber()).thenReturn(phone);
		when(userRepository.findByPhoneHash(any())).thenReturn(Optional.of(user));
		when(user.getId()).thenReturn(userId);

		// when
		UserIdResponseDto result = accountService.findUserId(dto);

		// then
		assertThat(result.id()).isNotBlank();
	}

	@Test
	@DisplayName("가입 이력이 없으면 ID 조회 시 예외가 발생한다.")
	void findUserId_fail() {
		UserVerifyRequestDto dto = new UserVerifyRequestDto("verifyId");

		PortOneVerifyInfoResponseDto responseDto = mock(PortOneVerifyInfoResponseDto.class);
		VerifiedCustomer customer = mock(VerifiedCustomer.class);

		when(portOneService.getPortOneVerifyInfo("verifyId")).thenReturn(responseDto);
		when(responseDto.verifiedCustomer()).thenReturn(customer);
		when(customer.phoneNumber()).thenReturn("01000000000");
		when(userRepository.findByPhoneHash(any())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> accountService.findUserId(dto))
			.isInstanceOf(EntityNotFoundException.class);
	}

	@Test
	@DisplayName("ID 존재 시 VERIFY 세션 생성 및 쿠키 발급이 정상 동작한다.")
	void existUser_success() {
		// given
		ExistUserRequestDto dto = new ExistUserRequestDto("testId");

		when(userRepository.existsById("testId")).thenReturn(true);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);

		// when
		accountService.existUser(response, dto);

		// then
		verify(valueOperations)
			.set(startsWith("VERIFY_SESSION:"), eq("testId"), any());
		verify(response).addHeader(eq("Set-Cookie"), anyString());
	}

	@Test
	@DisplayName("ID가 존재하지 않으면 예외가 발생한다.")
	void existUser_fail() {
		ExistUserRequestDto dto = new ExistUserRequestDto("testId");

		when(userRepository.existsById("testId")).thenReturn(false);

		assertThatThrownBy(() -> accountService.existUser(response, dto))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("본인 인증 후 비밀번호 재설정 토큰 발급이 정상 동작한다.")
	void verifyForResetPassword_success() {
		// given
		String sessionId = "session123";
		String userId = "testId";
		String phone = "01099998888";

		when(request.getCookies())
			.thenReturn(new Cookie[] {new Cookie("ex_u", sessionId)});

		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get("VERIFY_SESSION:" + sessionId)).thenReturn(userId);

		UserVerifyRequestDto dto = new UserVerifyRequestDto("verifyId");

		PortOneVerifyInfoResponseDto responseDto = mock(PortOneVerifyInfoResponseDto.class);
		VerifiedCustomer customer = mock(VerifiedCustomer.class);
		Users user = mock(Users.class);

		when(portOneService.getPortOneVerifyInfo("verifyId")).thenReturn(responseDto);
		when(responseDto.verifiedCustomer()).thenReturn(customer);
		when(customer.phoneNumber()).thenReturn(phone);
		when(userRepository.findByPhoneHash(any())).thenReturn(Optional.of(user));
		when(user.getId()).thenReturn(userId);

		// when
		accountService.verifyForResetPassword(request, response, dto);

		// then
		verify(valueOperations)
			.set(startsWith("RESET_PASSWORD:"), eq(userId), any());
		verify(redisTemplate).delete("VERIFY_SESSION:" + sessionId);
		verify(response, atLeastOnce()).addHeader(eq("Set-Cookie"), anyString());
	}

	@Test
	@DisplayName("비밀번호 재설정이 정상적으로 수행된다.")
	void resetPassword_success() {
		// given
		String resetToken = "reset123";
		String userId = "userId";
		String encodedPw = "encodedPw";

		when(request.getCookies())
			.thenReturn(new Cookie[] {new Cookie("rp_t", resetToken)});

		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get("RESET_PASSWORD:" + resetToken)).thenReturn(userId);

		Users user = mock(Users.class);
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(passwordEncoder.encode("newPassword")).thenReturn(encodedPw);

		ResetPasswordRequestDto dto = new ResetPasswordRequestDto("newPassword");

		// when
		accountService.resetPassword(request, response, dto);

		// then
		verify(user).updatePassword(encodedPw);
		verify(redisTemplate).delete("RESET_PASSWORD:" + resetToken);
		verify(response).addHeader(eq("Set-Cookie"), anyString());
	}

	@Test
	@DisplayName("로그인 상태에서 비밀번호 변경이 정상적으로 수행된다.")
	void resetPasswordMe_success() {
		// given
		String userNumber = "kakao_1234";
		String rawCurrentPw = "currentPw";
		String newPw = "newPw";
		String encodedNewPw = "encodedNewPw";

		CustomOAuth2User oauthUser = mock(CustomOAuth2User.class);
		Users user = mock(Users.class);

		when(oauthUser.getName()).thenReturn(userNumber);
		when(userRepository.findByUserNumber(userNumber)).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(rawCurrentPw, user.getPassword())).thenReturn(true);
		when(passwordEncoder.encode(newPw)).thenReturn(encodedNewPw);

		ResetPasswordMeRequestDto dto =
			new ResetPasswordMeRequestDto(rawCurrentPw, newPw);

		// when
		accountService.resetPasswordMe(oauthUser, dto);

		// then
		verify(user).updatePassword(encodedNewPw);
	}
}
