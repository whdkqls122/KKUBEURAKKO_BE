package com.kkubeurakko.kkubeurakko_be.domain.survey.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kkubeurakko.kkubeurakko_be.domain.survey.dto.SurveyRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.survey.entity.OptionType;
import com.kkubeurakko.kkubeurakko_be.domain.survey.entity.Survey;
import com.kkubeurakko.kkubeurakko_be.domain.survey.entity.SurveyOption;
import com.kkubeurakko.kkubeurakko_be.domain.survey.repository.SurveyRepository;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SurveyServiceImpl implements SurveyService {

	private final SurveyRepository surveyRepository;
	private final UserService userService;

	public boolean isSurveySubmitted(String userNumber) {
		Users user = userService.getUser(userNumber);
		return surveyRepository.existsByUser(user);
	}

	@Override
	@Transactional
	public void completeSurvey(String userNumber, SurveyRequestDto surveyRequestDto) {
		Users user = userService.getUser(userNumber);

		if (surveyRepository.existsByUser(user)) {
			throw new IllegalArgumentException("이미 설문에 참여했습니다.");
		}

		Survey survey = Survey.builder()
			.rewarded(false)
			.improvement(surveyRequestDto.improvement())
			.inconvenience(surveyRequestDto.inconvenience())
			.user(user)
			.createdAt(LocalDateTime.now())
			.build();

		for (OptionType optionType : surveyRequestDto.optionTypes()) {
			survey.addSurveyOption(
				SurveyOption.builder()
					.optionType(optionType)
					.build()
			);
		}
		log.info("설문 참여 완료");
		surveyRepository.save(survey);
	}
}
