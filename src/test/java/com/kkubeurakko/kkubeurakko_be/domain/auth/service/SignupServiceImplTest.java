package com.kkubeurakko.kkubeurakko_be.domain.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.CheckDuplicateDto;
import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.SignupRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.dto.TempUser;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.UserRole;
import com.kkubeurakko.kkubeurakko_be.domain.users.repository.UserRepository;
import com.kkubeurakko.kkubeurakko_be.global.ApiResponse;

import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class SignupServiceImplTest {
	@InjectMocks
	private SignupServiceImpl signupService;

	@Mock
	private TokenService tokenService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

	@Mock
	private ValueOperations<String, Object> valueOperations;

	@Mock
	private HttpServletResponse response;

	@Test
	@DisplayName("정상적으로 임시유저를 저장하고 토큰을 발급하는지 검증한다.")
	void signupInit() {
		// given
		SignupRequestDto dto = new SignupRequestDto(
			"testId",
			"password123!",
			"닉네임"
		);

		when(passwordEncoder.encode(any()))
			.thenReturn("encodedPassword");

		when(redisTemplate.opsForValue())
			.thenReturn(valueOperations);

		// when
		signupService.signupInit(response, dto);

		// then
		// Redis 저장 검증
		verify(valueOperations).set(
			startsWith("TEMP_USER:"),
			any(TempUser.class),
			eq(Duration.ofMinutes(10))
		);

		// 임시 토큰 생성 호출 검증
		verify(tokenService).createTempToken(eq(response), anyString());

		// SecurityContext 검증
		Authentication authentication =
			SecurityContextHolder.getContext().getAuthentication();

		assertNotNull(authentication);
		assertTrue(
			authentication.getAuthorities()
				.stream()
				.anyMatch(a -> a.getAuthority().equals(UserRole.TEMP.getRole()))
		);
	}

	@Test
	@DisplayName("중복이 없으면 true를 반환하는지 검증한다.")
	void checkDuplicatedId() {
		// given
		CheckDuplicateDto dto = new CheckDuplicateDto("newUser");

		when(userRepository.existsById("newUser"))
			.thenReturn(false);

		// when
		ApiResponse<Void> apiResponse = signupService.checkDuplicatedId(dto);

		// then
		assertTrue(apiResponse.isSuccess());
	}

	@Test
	@DisplayName("중복이 있으면 false를 반환하는지 검증한다.")
	void checkDuplicatedId_fail() {
		// given
		CheckDuplicateDto dto = new CheckDuplicateDto("existUser");

		when(userRepository.existsById("existUser"))
			.thenReturn(true);

		// when
		ApiResponse<Void> apiResponse = signupService.checkDuplicatedId(dto);

		// then
		assertFalse(apiResponse.isSuccess());
	}

}