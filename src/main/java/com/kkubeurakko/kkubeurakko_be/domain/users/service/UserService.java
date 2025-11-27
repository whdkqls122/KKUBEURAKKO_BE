package com.kkubeurakko.kkubeurakko_be.domain.users.service;

import org.springframework.transaction.annotation.Transactional;

import com.kkubeurakko.kkubeurakko_be.domain.users.dto.UserInfoResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

public interface UserService {
	@Transactional
	void saveUserPhone(String phone, CustomOAuth2User customOAuth2User);

	boolean existsUserPhone(String phone);

	Users getUser(String userNumber);

	//사용자 정보 조회
	UserInfoResponseDto getUserInfo(CustomOAuth2User customOAuth2User);

}
