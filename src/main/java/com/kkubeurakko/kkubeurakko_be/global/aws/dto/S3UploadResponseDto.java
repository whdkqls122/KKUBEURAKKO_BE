package com.kkubeurakko.kkubeurakko_be.global.aws.dto;

import lombok.Builder;

@Builder
public record S3UploadResponseDto(
	String imageUrl,
	String imageKey
) {
}