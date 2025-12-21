package com.kkubeurakko.kkubeurakko_be.domain.users.service;

import com.kkubeurakko.kkubeurakko_be.domain.users.dto.UserVerifyRequestDto;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

public interface UserVerifyFacadeService {
	//사용자 인증 정보 조회 후 휴대폰 번호 저장
	void verifyAndSavePhone(UserVerifyRequestDto userVerifyRequestDto, CustomOAuth2User customOAuth2User);

	void deleteUserAndUnlink(CustomOAuth2User customOAuth2User);
}
