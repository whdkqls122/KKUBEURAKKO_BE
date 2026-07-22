package com.kkubeurakko.kkubeurakko_be.domain.survey.scheduler;

import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kkubeurakko.kkubeurakko_be.domain.survey.repository.SurveyRepository;
import com.kkubeurakko.kkubeurakko_be.domain.survey.service.SurveyRewardService;

@ExtendWith(MockitoExtension.class)
class RewardSchedulerTest {

	@InjectMocks
	private RewardScheduler rewardScheduler;

	@Mock
	private SurveyRewardService surveyRewardService;

	@Mock
	private SurveyRepository surveyRepository;

	@Test
	@DisplayName("미지급 설문이 없으면 아무 작업도 하지 않는다.")
	void provideSurveyRewards_noPendingSurveys() {
		// given
		when(surveyRepository.findIdsByRewardedFalse()).thenReturn(List.of());

		// when
		rewardScheduler.provideSurveyRewards();

		// then
		verify(surveyRewardService, never()).provideSurveyReward(anyLong());
	}

	@Test
	@DisplayName("미지급 설문이 있으면 각 설문에 대해 보상 지급을 시도한다.")
	void provideSurveyRewards_success() {
		// given
		when(surveyRepository.findIdsByRewardedFalse()).thenReturn(List.of(1L, 2L, 3L));

		// when
		rewardScheduler.provideSurveyRewards();

		// then
		verify(surveyRewardService).provideSurveyReward(1L);
		verify(surveyRewardService).provideSurveyReward(2L);
		verify(surveyRewardService).provideSurveyReward(3L);
	}

	@Test
	@DisplayName("특정 설문 보상 지급에 실패해도 다음 설문은 계속 처리한다.")
	void provideSurveyRewards_continueWhenExceptionOccurs() {
		// given
		when(surveyRepository.findIdsByRewardedFalse()).thenReturn(List.of(1L, 2L, 3L));
		doThrow(new IllegalArgumentException("실패"))
			.when(surveyRewardService).provideSurveyReward(2L);

		// when
		rewardScheduler.provideSurveyRewards();

		// then
		verify(surveyRewardService).provideSurveyReward(1L);
		verify(surveyRewardService).provideSurveyReward(2L);
		verify(surveyRewardService).provideSurveyReward(3L);
	}

}