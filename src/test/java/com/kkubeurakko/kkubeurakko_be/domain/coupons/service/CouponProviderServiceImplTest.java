package com.kkubeurakko.kkubeurakko_be.domain.coupons.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.CouponCategory;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.Coupons;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.Status;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.UsersCoupons;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.repository.CouponRepository;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.repository.UserCouponRepository;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.util.GeneratedCouponNumberUtil;
import com.kkubeurakko.kkubeurakko_be.domain.survey.entity.Survey;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;

@ExtendWith(MockitoExtension.class)
class CouponProviderServiceImplTest {

	@InjectMocks
	private CouponProviderServiceImpl couponProviderService;

	@Mock
	private CouponRepository couponRepository;

	@Mock
	private UserCouponRepository userCouponRepository;

	@Mock
	private GeneratedCouponNumberUtil generatedCouponNumberUtil;

	@Test
	@DisplayName("설문 보상 쿠폰이 없으면 예외가 발생한다.")
	void provideSurveyRewardCoupon_couponNotFound() {
		// given
		Users user = Users.builder().userNumber("user123").build();
		Survey survey = mock(Survey.class);

		when(couponRepository.findByCouponCategoryAndStatus(CouponCategory.SURVEY_REWARD, Status.OFFER))
			.thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> couponProviderService.provideSurveyRewardCoupon(user, survey))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("설문 조사 보상 쿠폰이 없습니다.");
	}

	@Test
	@DisplayName("이미 지급된 사용자면 쿠폰을 저장하지 않는다.")
	void provideSurveyRewardCoupon_alreadyProvided() {
		// given
		Users user = Users.builder().userNumber("user123").build();
		Survey survey = mock(Survey.class);

		Coupons coupon = Coupons.builder()
			.id(1L)
			.name("설문 쿠폰")
			.quantity(10)
			.couponCategory(CouponCategory.SURVEY_REWARD)
			.status(Status.OFFER)
			.build();

		when(couponRepository.findByCouponCategoryAndStatus(CouponCategory.SURVEY_REWARD, Status.OFFER))
			.thenReturn(Optional.of(coupon));
		when(userCouponRepository.existsByUserUserNumberAndCouponId("user123", 1L))
			.thenReturn(true);

		// when
		couponProviderService.provideSurveyRewardCoupon(user, survey);

		// then
		verify(userCouponRepository, never()).save(any());
	}

	@Test
	@DisplayName("쿠폰 수량이 없으면 예외가 발생한다.")
	void provideSurveyRewardCoupon_noQuantity() {
		// given
		Users user = Users.builder().userNumber("user123").build();
		Survey survey = mock(Survey.class);

		Coupons coupon = Coupons.builder()
			.id(1L)
			.name("설문 쿠폰")
			.quantity(0)
			.couponCategory(CouponCategory.SURVEY_REWARD)
			.status(Status.OFFER)
			.build();

		when(couponRepository.findByCouponCategoryAndStatus(CouponCategory.SURVEY_REWARD, Status.OFFER))
			.thenReturn(Optional.of(coupon));
		when(userCouponRepository.existsByUserUserNumberAndCouponId("user123", 1L))
			.thenReturn(false);

		// when & then
		assertThatThrownBy(() -> couponProviderService.provideSurveyRewardCoupon(user, survey))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("설문 보상 쿠폰 수량이 부족합니다.");
	}

	@Test
	@DisplayName("정상적으로 설문 보상 쿠폰을 발급한다.")
	void provideSurveyRewardCoupon_success() {
		// given
		Users user = Users.builder().userNumber("user123").build();
		Survey survey = mock(Survey.class);

		Coupons coupon = Coupons.builder()
			.id(1L)
			.name("설문 쿠폰")
			.quantity(10)
			.couponCategory(CouponCategory.SURVEY_REWARD)
			.status(Status.OFFER)
			.build();

		when(couponRepository.findByCouponCategoryAndStatus(CouponCategory.SURVEY_REWARD, Status.OFFER))
			.thenReturn(Optional.of(coupon));
		when(userCouponRepository.existsByUserUserNumberAndCouponId("user123", 1L))
			.thenReturn(false);
		when(generatedCouponNumberUtil.generatedCouponNumber()).thenReturn("COUPON-1234");

		// when
		couponProviderService.provideSurveyRewardCoupon(user, survey);

		// then
		verify(userCouponRepository).save(any(UsersCoupons.class));
		assertThat(coupon.getQuantity()).isEqualTo(9);
	}
}