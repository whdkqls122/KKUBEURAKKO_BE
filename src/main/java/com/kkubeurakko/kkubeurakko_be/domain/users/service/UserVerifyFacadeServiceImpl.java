package com.kkubeurakko.kkubeurakko_be.domain.users.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.kkubeurakko.kkubeurakko_be.domain.auth.service.TokenService;
import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.PortOneVerifyInfoResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.portone.service.PortOneService;
import com.kkubeurakko.kkubeurakko_be.domain.users.dto.TempUserDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.dto.UserVerifyRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.exception.UserVerifyException;
import com.kkubeurakko.kkubeurakko_be.global.jwt.util.JwtUtil;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;
import com.kkubeurakko.kkubeurakko_be.global.oauth.exception.OAUthUnlinkException;
import com.kkubeurakko.kkubeurakko_be.global.oauth.service.OAuthUnlinkService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserVerifyFacadeServiceImpl implements UserVerifyFacadeService {
	private final PortOneService portOneService;
	private final UserService userService;
	private final OAuthUnlinkService oAuthUnlinkService;
	private final TokenService tokenService;
	private final RedisTemplate<String, Object> redisTemplate;
	private final JwtUtil jwtUtil;
	private static final String TEMP_USER_PREFIX_KEY = "TEMP_USER:";

	@Override
	public void verifyAndSaveUser(
		UserVerifyRequestDto userVerifyRequestDto,
		HttpServletRequest request,
		HttpServletResponse response
	) {
		String tempToken = tokenService.getTempToken(request);
		TempUserDto tempUserDto = (TempUserDto)redisTemplate.opsForValue()
			.get(TEMP_USER_PREFIX_KEY + jwtUtil.getUserNumber(tempToken));
		try {
			PortOneVerifyInfoResponseDto responseDto =
				portOneService.getPortOneVerifyInfo(userVerifyRequestDto.identityVerificationId());

			userService.createUser(tempUserDto, responseDto);

		} catch (Exception e) {
			try {
				tokenService.removeTempToken(response);
				oAuthUnlinkService.revokeOAuthAccount(tempUserDto.userNumber());
			} catch (Exception ex) {
				log.error("OAuth unlink 실패", ex);
			}

			throw new UserVerifyException(e.getMessage(), e);
		} finally {
			redisTemplate.delete(TEMP_USER_PREFIX_KEY + jwtUtil.getUserNumber(tempToken));
		}
	}

	@Override
	public void deleteUserAndUnlink(
		CustomOAuth2User customOAuth2User,
		HttpServletRequest request,
		HttpServletResponse response
	) {
		TempUserDto tempUserDto = (TempUserDto)redisTemplate.opsForValue()
			.get(TEMP_USER_PREFIX_KEY + customOAuth2User.getName());

		try {
			oAuthUnlinkService.revokeOAuthAccount(tempUserDto.userNumber());
		} catch (Exception e) {
			log.error("OAuth unlink 실패");
			throw new OAUthUnlinkException("탈퇴 중 오류 발생, 가게에 문의 부탁드립니다.", e);
		}
		redisTemplate.delete(TEMP_USER_PREFIX_KEY + customOAuth2User.getName());
	}
}
