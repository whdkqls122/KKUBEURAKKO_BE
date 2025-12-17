package com.kkubeurakko.kkubeurakko_be.domain.users.service;

import org.springframework.stereotype.Service;

import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.PortOneVerifyInfoResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.portone.service.PortOneService;
import com.kkubeurakko.kkubeurakko_be.domain.users.dto.UserVerifyRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserVerifyFacadeServiceImpl implements UserVerifyFacadeService {
	private final PortOneService portOneService;
	private final UserService userService;

	@Override
	public void verifyAndSavePhone(UserVerifyRequestDto userVerifyRequestDto, CustomOAuth2User customOAuth2User) {
		Users user = userService.getUser(customOAuth2User.getName());
		try {
			PortOneVerifyInfoResponseDto responseDto =
				portOneService.getPortOneVerifyInfo(userVerifyRequestDto.identityVerificationId());

			userService.saveUserPhone(user, responseDto);
		} catch (Exception e) {
			userService.deleteUser(user);

			try {
				// oauthUnlinkFacadeService.unlink(users);
			} catch (Exception ex) {
				// unlink 실패는 로그만 (재처리 대상)
				log.error("OAuth unlink 실패", ex);
			}

			throw e;
		}
	}
}
