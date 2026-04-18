package com.kkubeurakko.kkubeurakko_be.domain.survey.service;

public interface SurveyService {

	//설문 참여 여부 확인
	boolean hasParticipated(String userNumber);
}
