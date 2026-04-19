package com.kkubeurakko.kkubeurakko_be.domain.survey.service;

import com.kkubeurakko.kkubeurakko_be.domain.survey.dto.SurveyRequestDto;

public interface SurveyService {

	//설문 참여 여부 확인
	boolean isSurveySubmitted(String userNumber);

	void completeSurvey(String userNumber, SurveyRequestDto surveyRequestDto);
}
