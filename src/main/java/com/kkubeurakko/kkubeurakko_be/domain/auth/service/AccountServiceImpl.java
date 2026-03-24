package com.kkubeurakko.kkubeurakko_be.domain.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.ExistUserRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.NextStepDto;
import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.ResetPasswordMeRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.ResetPasswordRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.UserIdResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.auth.util.UserIdMaskingUtil;
import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.PortOneVerifyInfoResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.portone.service.PortOneService;
import com.kkubeurakko.kkubeurakko_be.domain.users.dto.UserVerifyRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.exception.PhoneHashingException;
import com.kkubeurakko.kkubeurakko_be.domain.users.repository.UserRepository;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {
	private final PortOneService portOneService;
	private final UserRepository userRepository;
	private final RedisTemplate<String, Object> redisTemplate;
	private final PasswordEncoder passwordEncoder;
	private final CookieService cookieService;
	private static final String VERIFY_SESSION_PREFIX = "VERIFY_SESSION:";
	private static final String RESET_PASSWORD_PREFIX = "RESET_PASSWORD:";

	private static final Duration VERIFY_SESSION_TTL = Duration.ofMinutes(5);
	private static final Duration RESET_PASSWORD_TTL = Duration.ofMinutes(5);

	private static final String VERIFY_COOKIE_PREFIX = "ex_u";
	private static final String RESET_COOKIE_PREFIX = "rp_t";

	@Override
	public UserIdResponseDto findUserId(UserVerifyRequestDto userVerifyRequestDto) {
		PortOneVerifyInfoResponseDto response =
			portOneService.getPortOneVerifyInfo(userVerifyRequestDto.identityVerificationId());

		String phone = response.verifiedCustomer().phoneNumber();
		String phoneHash = hashPhone(phone);
		Users users = userRepository.findByPhoneHash(phoneHash)
			.orElseThrow(() -> new EntityNotFoundException("가입된 이력이 없습니다."));

		return UserIdResponseDto.builder()
			.id(UserIdMaskingUtil.mask(users.getId()))
			.build();
	}

	@Override
	public void existUser(HttpServletResponse response, ExistUserRequestDto existUserRequestDto) {
		if (!userRepository.existsById(existUserRequestDto.id())) {
			throw new IllegalArgumentException("가입된 이력이 없습니다.");
		}
		String sessionId = UUID.randomUUID().toString();

		redisTemplate.opsForValue().set(
			VERIFY_SESSION_PREFIX + sessionId,
			existUserRequestDto.id(),
			VERIFY_SESSION_TTL
		);

		cookieService.addCookie(response, VERIFY_COOKIE_PREFIX, sessionId, VERIFY_SESSION_TTL, true);
	}

	@Override
	public NextStepDto getNextStep(HttpServletRequest request) {
		String verifyToken = cookieService.getCookieValue(request, VERIFY_COOKIE_PREFIX);
		if (verifyToken != null &&
			redisTemplate.hasKey(VERIFY_SESSION_PREFIX + verifyToken)) {

			return NextStepDto.builder()
				.activeTab("findPw")
				.pwStep("RESET")
				.build();
		}

		return NextStepDto.builder()
			.activeTab("findId")
			.pwStep("ENTER_ID")
			.build();
	}

	@Override
	public void verifyForResetPassword(
		HttpServletRequest request,
		HttpServletResponse response,
		UserVerifyRequestDto verifyRequestDto
	) {

		String verifyToken = cookieService.getCookieValue(request, VERIFY_COOKIE_PREFIX);
		if (verifyToken == null || verifyToken.isBlank()) {
			throw new IllegalStateException("아이디 확인 세션이 만료되었습니다.");
		}

		String sessionKey = VERIFY_SESSION_PREFIX + verifyToken;

		String inputId = (String)redisTemplate.opsForValue().get(sessionKey);

		if (inputId == null) {
			throw new IllegalStateException("아이디 확인 세션이 만료되었습니다.");
		}

		PortOneVerifyInfoResponseDto portOneVerifyInfoResponseDto =
			portOneService.getPortOneVerifyInfo(verifyRequestDto.identityVerificationId());

		String phoneHash = hashPhone(portOneVerifyInfoResponseDto.verifiedCustomer().phoneNumber());

		Users user = userRepository.findByPhoneHash(phoneHash)
			.orElseThrow(() -> new EntityNotFoundException("인증정보로 가입된 이력이 없습니다."));

		if (!user.getId().equals(inputId)) {
			throw new IllegalStateException("가입된 정보와 일치하지 않습니다.");
		}

		String resetToken = UUID.randomUUID().toString();

		redisTemplate.opsForValue().set(
			RESET_PASSWORD_PREFIX + resetToken,
			user.getId(),
			RESET_PASSWORD_TTL
		);

		cookieService.addCookie(response, RESET_COOKIE_PREFIX, resetToken, RESET_PASSWORD_TTL, true);

		redisTemplate.delete(sessionKey);
		cookieService.removeCookie(response, VERIFY_COOKIE_PREFIX, true);
	}

	@Override
	@Transactional
	public void resetPassword(HttpServletRequest request, HttpServletResponse response,
		ResetPasswordRequestDto resetPasswordRequestDto) {
		String resetToken = cookieService.getCookieValue(request, RESET_COOKIE_PREFIX);
		
		if (resetToken == null || resetToken.isBlank()) {
			throw new IllegalStateException("비밀번호 재설정 인증이 만료되었습니다.");
		}

		String key = RESET_PASSWORD_PREFIX + resetToken;

		String userId = (String)redisTemplate.opsForValue().get(key);

		if (userId == null) {
			throw new IllegalStateException("비밀번호 재설정 인증이 만료되었습니다.");
		}

		Users user = userRepository.findById(userId)
			.orElseThrow(() -> new EntityNotFoundException("존재하지 않는 사용자 입니다."));

		user.updatePassword(passwordEncoder.encode(resetPasswordRequestDto.newPassword()));

		redisTemplate.delete(key);
		cookieService.removeCookie(response, RESET_COOKIE_PREFIX, true);
	}

	@Override
	@Transactional
	public void resetPasswordMe(CustomOAuth2User customOAuth2User, ResetPasswordMeRequestDto resetPasswordRequestDto) {
		String userNumber = customOAuth2User.getName();
		Users user = userRepository.findByUserNumber(userNumber)
			.orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

		if (!passwordEncoder.matches(resetPasswordRequestDto.currentPassword(), user.getPassword())) {
			throw new IllegalStateException("올바른 비밀번호를 입력해주세요.");
		}

		user.updatePassword(passwordEncoder.encode(resetPasswordRequestDto.newPassword()));
	}

	private String hashPhone(String phone) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] encoded = digest.digest(phone.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(encoded);

		} catch (Exception e) {
			throw new PhoneHashingException("전화번호 해시 생성에 실패 하였습니다.", e);
		}
	}
}
