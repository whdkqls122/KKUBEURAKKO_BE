package com.kkubeurakko.kkubeurakko_be.domain.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
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
import com.kkubeurakko.kkubeurakko_be.domain.users.exception.UserVerifyException;
import com.kkubeurakko.kkubeurakko_be.domain.users.repository.UserRepository;

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

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("정상적으로 임시유저를 저장하고 토큰을 발급하는지 검증한다.")
	void signupInit() {
		// given
		SignupRequestDto dto = new SignupRequestDto(
			"testId",
			"test@email.com",
			"password123!",
			"닉네임"
		);

		when(passwordEncoder.encode("password123!"))
			.thenReturn("encodedPassword");
		when(redisTemplate.opsForValue())
			.thenReturn(valueOperations);

		// when
		signupService.signupInit(response, dto);

		// then
		verify(passwordEncoder).encode("password123!");
		verify(redisTemplate.opsForValue()).set(
			argThat(key -> key.startsWith("TEMP_USER:local_")),
			any(TempUser.class),
			eq(Duration.ofMinutes(10))
		);
		verify(tokenService).createTempToken(eq(response), argThat(userNumber ->
			userNumber != null && userNumber.startsWith("local_")
		));

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		assertNotNull(authentication);
		assertEquals(UserRole.TEMP.getRole(),
			authentication.getAuthorities().iterator().next().getAuthority());
		assertTrue(authentication.getName().startsWith("local_"));
	}

	@Test
	@DisplayName("회원가입 초기화 중 예외가 발생하면 UserVerifyException이 발생한다.")
	void signupInit_fail() {
		// given
		SignupRequestDto dto = new SignupRequestDto(
			"testId",
			"test@email.com",
			"닉네임",
			"password123!"
		);

		when(passwordEncoder.encode("password123!"))
			.thenThrow(new RuntimeException("encode fail"));

		// when & then
		assertThrows(UserVerifyException.class, () -> signupService.signupInit(response, dto));

		verify(redisTemplate, never()).opsForValue();
		verify(tokenService, never()).createTempToken(any(), anyString());
	}

	@Test
	@DisplayName("아이디가 중복되지 않으면 예외가 발생하지 않는다.")
	void checkDuplicatedId_success() {
		// given
		CheckDuplicateDto dto = new CheckDuplicateDto("newUser");
		when(userRepository.existsById("newUser")).thenReturn(false);

		// when & then
		assertDoesNotThrow(() -> signupService.checkDuplicatedId(dto));
		verify(userRepository).existsById("newUser");
	}

	@Test
	@DisplayName("아이디가 중복되면 예외가 발생한다.")
	void checkDuplicatedId_fail() {
		// given
		CheckDuplicateDto dto = new CheckDuplicateDto("existUser");
		when(userRepository.existsById("existUser")).thenReturn(true);

		// when & then
		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> signupService.checkDuplicatedId(dto)
		);

		assertEquals("이미 사용 중인 아이디입니다.", exception.getMessage());
		verify(userRepository).existsById("existUser");
	}
}