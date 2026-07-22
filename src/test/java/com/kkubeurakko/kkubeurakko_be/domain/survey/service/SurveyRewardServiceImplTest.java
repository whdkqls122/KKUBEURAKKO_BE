package com.kkubeurakko.kkubeurakko_be.domain.survey.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kkubeurakko.kkubeurakko_be.domain.coupons.service.CouponProviderService;
import com.kkubeurakko.kkubeurakko_be.domain.survey.entity.Survey;
import com.kkubeurakko.kkubeurakko_be.domain.survey.repository.SurveyRepository;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;

@ExtendWith(MockitoExtension.class)
class SurveyRewardServiceImplTest {

	@InjectMocks
	private SurveyRewardServiceImpl surveyRewardService;

	@Mock
	private SurveyRepository surveyRepository;

	@Mock
	private CouponProviderService couponProviderService;

	@Test
	@DisplayName("설문이 존재하지 않으면 예외가 발생한다.")
	void provideSurveyReward_notFound() {
		// given
		Long surveyId = 1L;
		when(surveyRepository.findById(surveyId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> surveyRewardService.provideSurveyReward(surveyId))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("설문이 존재하지 않습니다.");
	}

	@Test
	@DisplayName("이미 보상이 지급된 설문이면 쿠폰을 지급하지 않는다.")
	void provideSurveyReward_alreadyRewarded() {
		// given
		Long surveyId = 1L;
		Users user = Users.builder().userNumber("user123").build();

		Survey survey = Survey.builder()
			.rewarded(true)
			.user(user)
			.improvement("개선")
			.inconvenience("불편")
			.build();

		when(surveyRepository.findById(surveyId)).thenReturn(Optional.of(survey));

		// when
		surveyRewardService.provideSurveyReward(surveyId);

		// then
		verify(couponProviderService, never()).provideSurveyRewardCoupon(any(), any());
	}

	@Test
	@DisplayName("보상이 미지급된 설문이면 쿠폰을 지급하고 rewarded를 true로 변경한다.")
	void provideSurveyReward_success() {
		// given
		Long surveyId = 1L;
		Users user = Users.builder().userNumber("user123").build();

		Survey survey = Survey.builder()
			.rewarded(false)
			.user(user)
			.improvement("개선")
			.inconvenience("불편")
			.build();

		when(surveyRepository.findById(surveyId)).thenReturn(Optional.of(survey));

		// when
		surveyRewardService.provideSurveyReward(surveyId);

		// then
		verify(couponProviderService).provideSurveyRewardCoupon(user, survey);
		assertThat(survey.isRewarded()).isTrue();
	}
}