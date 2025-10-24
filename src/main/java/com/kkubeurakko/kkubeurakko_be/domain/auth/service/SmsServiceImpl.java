package com.kkubeurakko.kkubeurakko_be.domain.auth.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Service;

import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.SmsRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.SmsVerifyDto;
import com.kkubeurakko.kkubeurakko_be.domain.auth.entity.Sms;
import com.kkubeurakko.kkubeurakko_be.domain.auth.repository.SmsRepository;
import com.kkubeurakko.kkubeurakko_be.domain.auth.util.SmsCertificationUtil;
import com.kkubeurakko.kkubeurakko_be.domain.users.service.UserService;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsServiceImpl implements SmsService {
	private final SmsCertificationUtil smsCertificationUtil;
	private final SmsRepository smsRepository;
	private final UserService userService;
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

	@Override
	public void verifyCodeForCustomer(SmsVerifyDto smsVerifyDto, CustomOAuth2User customOAuth2User) {
		try {
			verifyPhone(smsVerifyDto);
			userService.saveUserPhone(smsVerifyDto.phoneNum(), customOAuth2User);
			log.info("인증 완료 후 사용자 전화번호 저장: {}", smsVerifyDto.phoneNum());
		} catch (NullPointerException e) {
			throw new RuntimeException("인증 실패");
		}
	}

	private String generateCertificationCode() {
		SecureRandom random = new SecureRandom();
		return String.format("%06d", random.nextInt(CERTIFICATION_CODE_BOUND));
	}

	private void verifyPhone(SmsVerifyDto smsVerifyDto) {
		if (!isVerify(smsVerifyDto.phoneNum(), smsVerifyDto.certificationCode())) {
			log.error("인증 실패 : {}", smsVerifyDto.phoneNum());
			throw new RuntimeException("휴대폰 인증에 실패하였습니다.");
		}
		smsRepository.deleteById(smsVerifyDto.phoneNum());
		log.info("인증 성공 : {}", smsVerifyDto.phoneNum());
	}

	private boolean isVerify(String phoneNum, String certificationCode) {
		if (!smsRepository.existsById(phoneNum)) {
			log.error("조회 실패 : {}", phoneNum);
			throw new RuntimeException("인증번호가 만료되었습니다.");
		}
		return smsRepository.findById(phoneNum)
			.orElseThrow(NullPointerException::new)
			.getCertificationCode()
			.equals(certificationCode);
	}
}
