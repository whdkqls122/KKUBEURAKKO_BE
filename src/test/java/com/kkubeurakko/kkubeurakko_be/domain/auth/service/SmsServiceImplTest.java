package com.kkubeurakko.kkubeurakko_be.domain.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.SmsRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.auth.dto.SmsVerifyDto;
import com.kkubeurakko.kkubeurakko_be.domain.auth.entity.Sms;
import com.kkubeurakko.kkubeurakko_be.domain.auth.repository.SmsRepository;
import com.kkubeurakko.kkubeurakko_be.domain.auth.util.SmsCertificationUtil;
import com.kkubeurakko.kkubeurakko_be.domain.users.service.UserService;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

@ExtendWith(MockitoExtension.class)
class SmsServiceImplTest {
	@Mock
	private SmsCertificationUtil smsCertificationUtil;

	@Mock
	private SmsRepository smsRepository;

	@Mock
	private UserService userService;

	@InjectMocks
	private SmsServiceImpl smsService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		smsService = new SmsServiceImpl(smsCertificationUtil, smsRepository, userService);
	}

	@Test
	@DisplayName("인증번호 전송 및 전화번호, 인증번호 저장이 정상적으로 이루어지는지 검증한다.")
	void sendSmsTest() {
		// given
		SmsRequestDto requestDto = new SmsRequestDto("010-8337-6023");

		// when
		smsService.sendSms(requestDto);

		// then
		verify(smsCertificationUtil, times(1)).sendSMS(anyString(), anyString());
		verify(smsRepository, times(1)).save(any(Sms.class));
	}

	@Test
	@DisplayName("인증번호 검증 및 휴대폰 번호 저장, 저장된 번호, 인증번호가 정상 삭제가 이루어지는지 검증한다.")
	void verifyCodeForCustomer() {
		// given
		SmsVerifyDto verifyDto = new SmsVerifyDto("010-8337-6023", "123456");
		CustomOAuth2User user = mock(CustomOAuth2User.class);
		Sms sms = Sms.builder()
			.phoneNum("010-8337-6023")
			.certificationCode("123456")
			.build();

		when(smsRepository.existsById("010-8337-6023")).thenReturn(true);
		when(smsRepository.findById("010-8337-6023")).thenReturn(Optional.of(sms));

		// when
		smsService.verifyCodeForCustomer(verifyDto, user);

		// then
		verify(userService, times(1)).saveUserPhone("010-8337-6023", user);
		verify(smsRepository, times(1)).deleteById("010-8337-6023");
	}

	@Test
	@DisplayName("인증번호가 일치하지 않는 경우 예외가 발생하는지 검증한다.")
	void verifyCodeForCustomerInvalidCertificationCode() {
		// given
		SmsVerifyDto verifyDto = new SmsVerifyDto("010-8337-6023", "999999");
		Sms sms = Sms.builder()
			.phoneNum("010-8337-6023")
			.certificationCode("123456")
			.build();

		when(smsRepository.existsById("010-8337-6023")).thenReturn(true);
		when(smsRepository.findById("010-8337-6023")).thenReturn(Optional.of(sms));

		// when & then
		assertThrows(RuntimeException.class, () -> {
			smsService.verifyCodeForCustomer(verifyDto, mock(CustomOAuth2User.class));
		});
	}

	@Test
	@DisplayName("인증번호가 만료된 경우 예외가 발생하는지 검증한다.")
	void verifyCodeForCustomerExpiredCertificationCode() {
		// given
		SmsVerifyDto verifyDto = new SmsVerifyDto("010-8337-6023", "123456");
		when(smsRepository.existsById("010-8337-6023")).thenReturn(false);

		// when & then
		assertThrows(RuntimeException.class, () -> {
			smsService.verifyCodeForCustomer(verifyDto, mock(CustomOAuth2User.class));
		});
	}
}