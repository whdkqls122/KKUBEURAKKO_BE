package com.kkubeurakko.kkubeurakko_be.domain.coupons.config;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.CouponCategory;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.Status;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.repository.CouponRepository;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.service.CouponProviderService;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.service.CouponService;

@ExtendWith(MockitoExtension.class)
class CouponSchedulerTest {

	@InjectMocks
	private CouponScheduler couponScheduler;

	@Mock
	private CouponRepository couponRepository;

	@Mock
	private CouponService couponService;

	@Mock
	private CouponProviderService couponProviderService;

	@Test
	@DisplayName("GIFT 쿠폰이 없으면 자동 생성한다")
	void createGiftCouponIfNotExists() {
		// given
		when(couponRepository.updateExpiredCoupons(any(), any()))
			.thenReturn(0);

		when(couponRepository.existsByCouponCategoryAndStatus(
			CouponCategory.GIFT,
			Status.OFFER
		)).thenReturn(false);

		// when
		couponScheduler.refreshCoupons();

		// then
		verify(couponService).createGiftCoupon(1L);
	}

	@Test
	@DisplayName("GIFT 쿠폰이 이미 존재하면 생성하지 않는다")
	void doNotCreateGiftCouponIfExists() {
		// given
		when(couponRepository.updateExpiredCoupons(any(), any()))
			.thenReturn(0);

		when(couponRepository.existsByCouponCategoryAndStatus(
			CouponCategory.GIFT,
			Status.OFFER
		)).thenReturn(true);

		// when
		couponScheduler.refreshCoupons();

		// then
		verify(couponService, never()).createGiftCoupon(anyLong());
	}

	@Test
	@DisplayName("GIFT 쿠폰 자동 지급 스케줄러 실행")
	void issueGiftCoupons() {
		// when
		couponScheduler.issuedGiftCoupons();

		// then
		verify(couponProviderService).provideGiftCoupon();
	}
}