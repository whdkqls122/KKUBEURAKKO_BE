package com.kkubeurakko.kkubeurakko_be.domain.coupons.service;

import com.kkubeurakko.kkubeurakko_be.domain.survey.entity.Survey;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;

public interface CouponProviderService {
	void provideSurveyRewardCoupon(Users user, Survey survey);

	void provideGiftCoupon();
}
