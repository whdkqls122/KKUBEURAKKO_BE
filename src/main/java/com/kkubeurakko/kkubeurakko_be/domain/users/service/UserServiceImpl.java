package com.kkubeurakko.kkubeurakko_be.domain.users.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Orders;
import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.PortOneVerifyInfoResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.dto.LocalTempUserDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.dto.OAuthTempUserDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.dto.TempUser;
import com.kkubeurakko.kkubeurakko_be.domain.users.dto.UserInfoResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.UserRole;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.exception.DuplicatePhoneException;
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
	public void createUser(TempUser tempUser, PortOneVerifyInfoResponseDto responseDto) {
		if (!"VERIFIED".equals(responseDto.status())) {
			throw new IllegalStateException("본인인증 실패");
		}
		String phone = responseDto.verifiedCustomer().phoneNumber();
		String phoneHash = hashPhone(phone);
		try {
			if (userRepository.existsByPhoneHash(phoneHash)) {
				log.error("중복된 사용자 전화 번호");
				throw new IllegalStateException();
			}

			Users.UsersBuilder builder = Users.builder()
				.userNumber(tempUser.getUserNumber())
				.nickname(tempUser.getNickname())
				.role(UserRole.CUSTOMER)
				.phone(aesUtil.encryptToBase64(phone))
				.phoneHash(phoneHash);

			if (tempUser instanceof OAuthTempUserDto oauthUser) {
				builder.email(oauthUser.getEmail());
			} else if (tempUser instanceof LocalTempUserDto localUser) {
				builder.email(localUser.getEmail());
				builder.id(localUser.getId());
				builder.password(localUser.getPasswordHash());
			}

			userRepository.save(builder.build());

		} catch (IllegalStateException e) {
			throw new DuplicatePhoneException("이미 가입된 계정이 존재합니다.", e);
		}

	}

	@Override
	public UserInfoResponseDto getUserInfo(CustomOAuth2User customOAuth2User) {
		Users user = getUser(customOAuth2User.getName());
		return toResponseDto(user);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void deleteUser(Users user) {
		if (!user.getOrdersList().isEmpty()) {
			user.getOrdersList().forEach(Orders::revokeUser);
		}
		userRepository.deleteByUserNumber(user.getUserNumber());
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
		String phone = aesUtil.decryptFromBase64(user.getPhone());
		// String maskingPhone = PhoneMaskingUtil.mask(phone);

		return UserInfoResponseDto.builder()
			.provider(getProvider(user.getUserNumber()))
			.email(user.getEmail())
			.nickname(user.getNickname())
			.phone(phone)
			.build();
	}

	private String getProvider(String userNumber) {
		return userNumber.substring(0, userNumber.indexOf("_"));
	}
}
