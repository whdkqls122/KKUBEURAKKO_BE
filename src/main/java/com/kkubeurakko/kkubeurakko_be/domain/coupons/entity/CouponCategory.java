package com.kkubeurakko.kkubeurakko_be.domain.coupons.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CouponCategory {
	GENERAL("일반 쿠폰"),
	WELCOME("신규 가입 쿠폰"),
	SURVEY_REWARD("설문 참여 쿠폰"),
	GIFT("서비스 이용 감사 쿠폰"),
	EVENT("이벤트 쿠폰");

	private final String category;
}
