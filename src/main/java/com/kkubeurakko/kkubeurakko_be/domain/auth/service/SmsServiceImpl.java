package com.kkubeurakko.kkubeurakko_be.domain.auth.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Service;

import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.SmsRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.auth.entity.Sms;
import com.kkubeurakko.kkubeurakko_be.domain.auth.repository.SmsRepository;
import com.kkubeurakko.kkubeurakko_be.domain.auth.util.SmsCertificationUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsServiceImpl implements SmsService {
	private final SmsCertificationUtil smsCertificationUtil;
	private final SmsRepository smsRepository;
	private final int CERTIFICATION_CODE_BOUND = 1_000_000;

	@Override
	public void sendSms(SmsRequestDto smsRequestDto) {
		try {
			String phoneNum = smsRequestDto.phoneNum();
			String certificationCode = generateCertificationCode();
			Sms sms = Sms.builder()
				.phoneNum(phoneNum)
				.certificationCode(certificationCode)
				.build();
			smsCertificationUtil.sendSMS(sms.getPhoneNum(), sms.getCertificationCode());

			smsRepository.save(sms);
			log.info("인증번호 발송 및 저장 완료 : {}", phoneNum);
		} catch (Exception e) {
			throw new RuntimeException("잔액 부족");
		}
	}

	private String generateCertificationCode() {
		SecureRandom random = new SecureRandom();
		return String.format("%06d", random.nextInt(CERTIFICATION_CODE_BOUND));
	}

}
