package com.kkubeurakko.kkubeurakko_be.domain.survey.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kkubeurakko.kkubeurakko_be.domain.coupons.service.CouponProviderService;
import com.kkubeurakko.kkubeurakko_be.domain.survey.entity.Survey;
import com.kkubeurakko.kkubeurakko_be.domain.survey.repository.SurveyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SurveyRewardServiceImpl implements SurveyRewardService {

	private final SurveyRepository surveyRepository;
	private final CouponProviderService couponProviderService;

	@Transactional
	@Override
	public void provideSurveyReward(Long surveyId) {
		Survey survey = surveyRepository.findById(surveyId)
			.orElseThrow(() -> new IllegalArgumentException("설문이 존재하지 않습니다."));

		if (survey.isRewarded()) {
			return;
		}

		couponProviderService.provideSurveyRewardCoupon(survey.getUser(), survey);
		survey.markRewarded();
	}
}