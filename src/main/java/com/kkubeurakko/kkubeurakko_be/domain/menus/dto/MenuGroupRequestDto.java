package com.kkubeurakko.kkubeurakko_be.domain.menus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record MenuGroupRequestDto(

	@NotBlank(message = "메뉴 그룹 이름은 필수입니다.")
	@Size(max = 30, message = "메뉴 그룹 이름은 최대 30자까지 가능합니다.")
	String name,

	@Size(max = 50, message = "설명은 최대 50자까지 가능합니다.")
	String description
) {
}
