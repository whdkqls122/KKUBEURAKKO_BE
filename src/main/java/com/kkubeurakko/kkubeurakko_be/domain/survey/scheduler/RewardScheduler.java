package com.kkubeurakko.kkubeurakko_be.domain.survey.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kkubeurakko.kkubeurakko_be.domain.survey.repository.SurveyRepository;
import com.kkubeurakko.kkubeurakko_be.domain.survey.service.SurveyRewardService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RewardScheduler {

	private final SurveyRewardService surveyRewardService;
	private final SurveyRepository surveyRepository;

	@Scheduled(fixedDelay = 300000)
	public void provideSurveyRewards() {
		List<Long> surveyIds = surveyRepository.findIdsByRewardedFalse();

		if (surveyIds.isEmpty()) {
			return;
		}

		for (Long surveyId : surveyIds) {
			try {
				surveyRewardService.provideSurveyReward(surveyId);
			} catch (Exception e) {
				log.error("설문 보상 지급 실패", e);
			}
		}
	}
}
