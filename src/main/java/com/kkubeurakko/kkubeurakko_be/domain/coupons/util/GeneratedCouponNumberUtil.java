package com.kkubeurakko.kkubeurakko_be.domain.coupons.util;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.kkubeurakko.kkubeurakko_be.domain.coupons.repository.UserCouponRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GeneratedCouponNumberUtil {
	private final UserCouponRepository userCouponRepository;

	public String generatedCouponNumber() {
		int maxAttempts = 10;
		for (int i = 0; i < maxAttempts; i++) {
			String couponNumber = UUID.randomUUID().toString().replace("-", "").substring(0, 19);
			if (!userCouponRepository.existsByCouponsNumber(couponNumber)) {
				return couponNumber;
			}
		}
		throw new IllegalStateException("다시 요청 해주세요");
	}
}
