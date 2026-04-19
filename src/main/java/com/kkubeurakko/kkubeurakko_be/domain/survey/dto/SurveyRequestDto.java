package com.kkubeurakko.kkubeurakko_be.domain.survey.dto;

import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.survey.entity.OptionType;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SurveyRequestDto(
	@NotEmpty(message = "설문조사 답변은 필수입니다.")
	@Size(min = 2, max = 150, message = "2 ~ 150자까지 입력 가능합니다.")
	String improvement,
	@NotEmpty(message = "불편사항을 1개 이상 선택해주세요.")
	List<@NotNull(message = "올바르지 않은 선택입니다.") OptionType> optionTypes,
	@NotEmpty(message = "설문조사 답변은 필수입니다.")
	@Size(min = 2, max = 150, message = "2 ~ 150자까지 입력 가능합니다.")
	String inconvenience
) {
}
