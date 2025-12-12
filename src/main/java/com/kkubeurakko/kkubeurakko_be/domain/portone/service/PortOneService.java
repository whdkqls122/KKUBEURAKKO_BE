package com.kkubeurakko.kkubeurakko_be.domain.portone.service;

import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.PortOnePaymentResponseDto;

public interface PortOneService {
	//포트원 결제 내역 조회
	PortOnePaymentResponseDto getPortOnePayment(String paymentId);

	//포트원 결제 취소 요청 메서드
	void cancelPortOnePayment(String paymentId);
}
