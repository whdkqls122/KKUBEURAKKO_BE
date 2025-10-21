package com.kkubeurakko.kkubeurakko_be.domain.auth.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.service.DefaultMessageService;

import jakarta.annotation.PostConstruct;

@Component
public class SmsCertificationUtil {
	@Value("${spring.coolsms.api-key}")
	private String apiKey;
	@Value("${spring.coolsms.api-secret}")
	private String apiSecret;
	@Value("${spring.coolsms.from-number}")
	private String fromNumber;
	@Value("${spring.coolsms.api-url}")
	private String apiUrl;

	DefaultMessageService messageService; //메시지 서비스 객체

	@PostConstruct
	public void init() {
		this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, apiUrl);
	}

	public void sendSMS(String to, String certificationCode) {
		Message message = new Message();
		message.setFrom(fromNumber);    //발신자 번호
		message.setTo(to);
		message.setText("본인확인 인증번호는 " + certificationCode + "입니다."); //메시지 내용
		this.messageService.sendOne(new SingleMessageSendingRequest(message)); //메시지 발송
	}
}