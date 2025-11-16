package com.kkubeurakko.kkubeurakko_be.domain.menus.dto;

public record MenuSummaryDto(
	Long id,
	String name,
	String description,
	int price,
	String img,
	String status
) {
}
