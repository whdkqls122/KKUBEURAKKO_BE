package com.kkubeurakko.kkubeurakko_be.domain.users.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kkubeurakko.kkubeurakko_be.domain.users.dto.UserInfoResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.exception.PhoneHashingException;
import com.kkubeurakko.kkubeurakko_be.domain.users.repository.UserRepository;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;
import com.kkubeurakko.kkubeurakko_be.global.security.util.AesUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
	private final AesUtil aesUtil;
	private final UserRepository userRepository;

	@Override
	@Transactional
	public void saveUserPhone(String phone, CustomOAuth2User customOAuth2User) {
		String phoneHash = hashPhone(phone);
		try {
			if (existsUserPhone(phoneHash)) {
				//중복된 전화번호
				throw new IllegalArgumentException("이미 저장된 전화번호");
			}
			String encodePhone = aesUtil.encryptToBase64(phone);
			Users users = userRepository
				.findByUserNumber(customOAuth2User.getName())
				.orElseThrow(NullPointerException::new);
			users.updateUsersPhone(phoneHash, encodePhone);
		} catch (RuntimeException e) {
			throw new RuntimeException("이미 저장된 전화번호 입니다.");
		}
	}

	@Override
	public UserInfoResponseDto getUserInfo(CustomOAuth2User customOAuth2User) {
		Users user = getUser(customOAuth2User.getName());
		return toResponseDto(user);
	}

	@Override
	public boolean existsUserPhone(String phonePhone) {
		return userRepository.existsByPhoneHash(phonePhone);
	}

	@Override
	public Users getUser(String userNumber) {
		return userRepository.findByUserNumber(userNumber)
			.orElseThrow(() -> new IllegalArgumentException("해당 사용자 정보가 없습니다."));
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

	private UserInfoResponseDto toResponseDto(Users user) {
		return UserInfoResponseDto.builder()
			.provider(getProvider(user.getUserNumber()))
			.email(user.getEmail())
			.nickname(user.getNickname())
			.build();
	}

	private String getProvider(String userNumber) {
		return userNumber.substring(0, userNumber.indexOf("_"));
	}
}
