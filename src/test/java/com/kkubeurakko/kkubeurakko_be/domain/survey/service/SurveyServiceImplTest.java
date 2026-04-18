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
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.service.UserService;

@ExtendWith(MockitoExtension.class)
class SurveyServiceImplTest {

	@Mock
	private SurveyRepository surveyRepository;

	@Mock
	private UserService userService;

	@InjectMocks
	private SurveyServiceImpl surveyService;

	@Test
	@DisplayName("설문에 참여한 유저면 true를 반환한다")
	void isSurveySubmitted_true() {

		// given
		String userNumber = "user123";

		Users user = Users.builder()
			.userNumber(userNumber)
			.build();

		when(userService.getUser(userNumber))
			.thenReturn(user);

		when(surveyRepository.existsByUser(user))
			.thenReturn(true);

		// when
		boolean result = surveyService.isSurveySubmitted(userNumber);

		// then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("설문에 참여하지 않은 유저면 false를 반환한다")
	void isSurveySubmitted_false() {

		// given
		String userNumber = "user123";

		Users user = Users.builder()
			.userNumber(userNumber)
			.build();

		when(userService.getUser(userNumber))
			.thenReturn(user);

		when(surveyRepository.existsByUser(user))
			.thenReturn(false);

		// when
		boolean result = surveyService.isSurveySubmitted(userNumber);

		// then
		assertThat(result).isFalse();
	}
}