package com.kkubeurakko.kkubeurakko_be.domain.portone.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PortOneErrorType {
	PAYMENT_ALREADY_CANCELLED(ErrorAction.SUCCESS), //결제가 이미 취소된 경우, 재시도 필요 X
	PAYMENT_NOT_FOUND(ErrorAction.SUCCESS), // 결제 정보가 없는 경우, 재시도 필요 X -> 결과가 같음
	PAYMENT_NOT_PAID(ErrorAction.SUCCESS), // 결제가 완료 되지 않은 경우, 재시도 필요 X

	INVALID_REQUEST(ErrorAction.FAIL), // 잘못된 요청

	PG_PROVIDER(ErrorAction.RETRY), // PG사 제공 오류, 서버 통신 에러 등

	UNKNOWN(ErrorAction.RETRY);

	private final ErrorAction action;

	public static PortOneErrorType from(String type) {
		try {
			return PortOneErrorType.valueOf(type);
		} catch (Exception e) {
			return UNKNOWN;
		}
	}
}
