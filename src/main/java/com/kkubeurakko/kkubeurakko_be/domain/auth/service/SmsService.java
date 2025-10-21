package com.kkubeurakko.kkubeurakko_be.domain.auth.service;

import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.SmsRequestDto;

public interface SmsService {
	void sendSms(SmsRequestDto smsRequestDto);
}
