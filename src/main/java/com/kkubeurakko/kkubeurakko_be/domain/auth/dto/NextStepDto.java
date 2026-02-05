package com.kkubeurakko.kkubeurakko_be.domain.auth.dto;

import lombok.Builder;

@Builder
public record NextStepDto(
	String activeTab,
	String pwStep
) {
}
