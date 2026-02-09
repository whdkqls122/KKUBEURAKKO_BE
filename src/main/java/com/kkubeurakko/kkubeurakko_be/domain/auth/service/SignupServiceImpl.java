package com.kkubeurakko.kkubeurakko_be.domain.auth.service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.CheckDuplicateDto;
import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.SignupRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.dto.LocalTempUserDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.dto.TempUser;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.UserRole;
import com.kkubeurakko.kkubeurakko_be.domain.users.exception.UserVerifyException;
import com.kkubeurakko.kkubeurakko_be.domain.users.repository.UserRepository;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignupServiceImpl implements SignupService {
	private final TokenService tokenService;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final RedisTemplate<String, Object> redisTemplate;
	private static final String TEMP_USER_KEY_PREFIX = "TEMP_USER:";
	private static final String LOCAL_PREFIX = "local_";

	@Override
	public void signupInit(HttpServletResponse response, SignupRequestDto signupRequestDto) {
		String userNumber = generateLocalUserNumber();
		try {
			TempUser tempUser = LocalTempUserDto.builder()
				.userNumber(userNumber)
				.id(signupRequestDto.id())
				.email(signupRequestDto.email())
				.nickname(signupRequestDto.nickname())
				.passwordHash(passwordEncoder.encode(signupRequestDto.password()))
				.build();

			Authentication authentication =
				new UsernamePasswordAuthenticationToken(
					tempUser.getUserNumber(),
					null,
					List.of(new SimpleGrantedAuthority(UserRole.TEMP.getRole()))
				);

			SecurityContextHolder.getContext().setAuthentication(authentication);

			redisTemplate.opsForValue().set(
				TEMP_USER_KEY_PREFIX + userNumber,
				tempUser,
				Duration.ofMinutes(10)
			);
			tokenService.createTempToken(response, userNumber);
		} catch (Exception e) {
			throw new UserVerifyException(e.getMessage(), e);
		}
	}

	@Override
	public void checkDuplicatedId(CheckDuplicateDto checkDuplicateDto) {
		if (userRepository.existsById(checkDuplicateDto.id())) {
			throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
		}
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
