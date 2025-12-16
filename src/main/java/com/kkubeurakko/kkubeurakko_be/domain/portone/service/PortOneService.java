package com.kkubeurakko.kkubeurakko_be.domain.portone.service;

import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.PortOnePaymentResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.PortOneVerifyInfoResponseDto;

public interface PortOneService {
	//포트원 결제 내역 조회
	PortOnePaymentResponseDto getPortOnePayment(String paymentId);

	//포트원 본인인증 내역 조회
	PortOneVerifyInfoResponseDto getPortOneVerifyInfo(String identityVerificationId);

	//포트원 결제 취소 요청 메서드
	void cancelPortOnePayment(String paymentId);
}
