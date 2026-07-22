package com.kkubeurakko.kkubeurakko_be.domain.portone.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.CashReceiptRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.PortOnePaymentResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.PortOneVerifyInfoResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;

public interface PortOneService {
	//포트원 결제 내역 조회
	PortOnePaymentResponseDto getPortOnePayment(String paymentId);

	//포트원 본인인증 내역 조회
	PortOneVerifyInfoResponseDto getPortOneVerifyInfo(String identityVerificationId);

	//포트원 결제 취소 요청 메서드
	void cancelPortOnePayment(String paymentId);

	//포트원 결제 취소 재시도 요청 메서드
	void retryCancelPortOnePayment(String paymentId) throws JsonProcessingException;

	//현금 영수증 발급
	void issueCashReceipt(Users user, String paymentId, CashReceiptRequestDto cashReceiptRequestDto, int totalAmount);

	//현금 영수증 발급 취소
	void cancelIssueCashReceipt(String paymentId);

	//현금 영수증 발급 취소 재시도 요청 메서드
	void retryCancelIssueCashReceipt(String paymentId);
}
