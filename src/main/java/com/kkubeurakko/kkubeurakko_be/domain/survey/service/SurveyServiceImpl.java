package com.kkubeurakko.kkubeurakko_be.domain.survey.service;

import org.springframework.stereotype.Service;

import com.kkubeurakko.kkubeurakko_be.domain.survey.repository.SurveyRepository;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SurveyServiceImpl {

	private final SurveyRepository surveyRepository;
	private final UserService userService;

	public boolean isSurveySubmitted(String userNumber) {
		Users user = userService.getUser(userNumber);
		return surveyRepository.existsByUser(user);
	}
}
