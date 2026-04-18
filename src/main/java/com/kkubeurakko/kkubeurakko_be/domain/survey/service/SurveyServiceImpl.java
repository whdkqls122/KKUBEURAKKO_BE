package com.kkubeurakko.kkubeurakko_be.domain.survey.service;

import org.springframework.stereotype.Service;

import com.kkubeurakko.kkubeurakko_be.domain.survey.repository.SurveyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SurveyServiceImpl {

	private final SurveyRepository surveyRepository;

	public boolean hasParticipated(String userNumber) {
		return surveyRepository.existsByUser_UserNumber(userNumber);
	}
}
