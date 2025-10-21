package com.kkubeurakko.kkubeurakko_be.domain.auth.service;

import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.SmsRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.SmsVerifyDto;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

public interface SmsService {
	void sendSms(SmsRequestDto smsRequestDto);

	void verifyCodeForCustomer(SmsVerifyDto smsVerifyDto, CustomOAuth2User customOAuth2User);
}
