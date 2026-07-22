package com.kkubeurakko.kkubeurakko_be.domain.menus.dto;

import lombok.Builder;

@Builder
public record ConvertedImageDto(
	byte[] data,
	String contentType,
	String extension
) {
}
