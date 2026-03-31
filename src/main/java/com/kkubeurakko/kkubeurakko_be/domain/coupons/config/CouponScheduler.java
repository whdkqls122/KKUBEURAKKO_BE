package com.kkubeurakko.kkubeurakko_be.domain.coupons.config;

import java.time.LocalDate;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.Status;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.repository.CouponRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CouponScheduler {

	private final CouponRepository couponRepository;

	// 1분마다 실행 (테스트용)
	// @Scheduled(fixedRate = 60000)
	@Scheduled(cron = "0 0 0 * * *")
	@Transactional
	public void hideExpiredCoupons() {
		LocalDate today = LocalDate.now();
		int updatedCount = couponRepository.updateExpiredCoupons(Status.EXHAUSTED, today);

		System.out.println("만료 쿠폰 숨김 처리 개수: " + updatedCount);
	}
}