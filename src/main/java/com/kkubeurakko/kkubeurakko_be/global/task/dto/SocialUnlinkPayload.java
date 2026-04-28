package com.kkubeurakko.kkubeurakko_be.global.task.dto;

import lombok.Builder;

@Builder
public record SocialUnlinkPayload(
	String userNumber,
	String provider
) {
}
