package com.kkubeurakko.kkubeurakko_be.domain.survey.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kkubeurakko.kkubeurakko_be.domain.survey.repository.SurveyRepository;

@ExtendWith(MockitoExtension.class)
class SurveyServiceImplTest {

	@Mock
	private SurveyRepository surveyRepository;

	@InjectMocks
	private SurveyServiceImpl surveyService;

	@Test
	@DisplayName("설문에 참여한 유저면 true를 반환한다")
	void hasParticipated_true() {

		// given
		String userNumber = "user123";

		when(surveyRepository.existsByUser_UserNumber(userNumber))
			.thenReturn(true);

		// when
		boolean result = surveyService.hasParticipated(userNumber);

		// then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("설문에 참여하지 않은 유저면 false를 반환한다")
	void hasParticipated_false() {

		// given
		String userNumber = "user123";

		when(surveyRepository.existsByUser_UserNumber(userNumber))
			.thenReturn(false);

		// when
		boolean result = surveyService.hasParticipated(userNumber);

		// then
		assertThat(result).isFalse();
	}
}