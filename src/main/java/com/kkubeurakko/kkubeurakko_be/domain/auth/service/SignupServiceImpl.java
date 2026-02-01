package com.kkubeurakko.kkubeurakko_be.domain.auth.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.CheckDuplicateDto;
import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.SignupRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.dto.LocalTempUserDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.dto.TempUser;
import com.kkubeurakko.kkubeurakko_be.domain.users.exception.UserVerifyException;
import com.kkubeurakko.kkubeurakko_be.domain.users.repository.UserRepository;
import com.kkubeurakko.kkubeurakko_be.global.security.util.AesUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignupServiceImpl implements SignupService {
	private final AesUtil aesUtil;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final LoginService loginService;
	private final RedisTemplate<String, Object> redisTemplate;
	private static final String TEMP_USER_KEY_PREFIX = "TEMP_USER:";
	private static final String LOCAL_PREFIX = "local_";

	@Override
	public void signupInit(SignupRequestDto signupRequestDto) {
		String userNumber = generateLocalUserNumber();
		try {
			TempUser tempUser = LocalTempUserDto.builder()
				.userNumber(userNumber)
				.nickname(signupRequestDto.nickname())
				.passwordHash(passwordEncoder.encode(signupRequestDto.password()))
				.build();

			redisTemplate.opsForValue().set(
				TEMP_USER_KEY_PREFIX + userNumber,
				tempUser,
				Duration.ofMinutes(10)
			);
		} catch (Exception e) {
			throw new UserVerifyException(e.getMessage(), e);
		}
	}

	@Override
	public boolean checkDuplicatedId(CheckDuplicateDto checkDuplicateDto) {
		return !userRepository.existsById(checkDuplicateDto.id());
	}

	//userNumber 생성
	private String generateLocalUserNumber() {
		String random = UUID.randomUUID()
			.toString()
			.replace("-", "")
			.substring(0, 10);

		return LOCAL_PREFIX + random;
	}
}
