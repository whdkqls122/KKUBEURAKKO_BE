package com.kkubeurakko.kkubeurakko_be.domain.users.service;

import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.PortOneVerifyInfoResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.dto.TempUserDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.dto.UserInfoResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

public interface UserService {
	void createUser(TempUserDto tempUserDto, PortOneVerifyInfoResponseDto responseDto);

	Users getUser(String userNumber);

	//사용자 정보 조회
	UserInfoResponseDto getUserInfo(CustomOAuth2User customOAuth2User);

	void deleteUser(Users user);
}
