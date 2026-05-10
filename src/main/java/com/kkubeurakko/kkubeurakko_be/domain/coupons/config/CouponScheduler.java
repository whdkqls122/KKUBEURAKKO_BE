package com.kkubeurakko.kkubeurakko_be.domain.coupons.config;

import java.time.LocalDate;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.CouponCategory;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.Status;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.repository.CouponRepository;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.service.CouponProviderService;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.service.CouponService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CouponScheduler {

	private final CouponRepository couponRepository;
	private final CouponService couponService;
	private final CouponProviderService couponProviderService;

	// 1분마다 실행 (테스트용)
	@Scheduled(fixedRate = 60000)
	// @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
	@Transactional
	public void refreshCoupons() {
		LocalDate today = LocalDate.now();
		int updatedCount = couponRepository.updateExpiredCoupons(Status.EXHAUSTED, today);

		log.info("만료 쿠폰 숨김 처리 개수: {}", updatedCount);

		if (!couponRepository.existsByCouponCategoryAndStatus(
			CouponCategory.GIFT,
			Status.OFFER
		)) {
			couponService.createGiftCoupon(1L);
			log.info("GIFT 쿠폰 생성 완료");
		}
	}

	@Scheduled(fixedDelay = 300000)
	@Transactional
	public void issuedGiftCoupons() {
		couponProviderService.provideGiftCoupon();
	}
}