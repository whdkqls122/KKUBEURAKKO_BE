package com.kkubeurakko.kkubeurakko_be.domain.menus.dto;

import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record AdminMenuUpdateDto(
	@NotBlank(message = "메뉴 이름은 필수입니다.")
	@Size(max = 30, message = "메뉴 이름은 최대 24자까지 가능합니다.")
	String name,

	@Size(max = 30, message = "메뉴 이름은 최대 120자까지 가능합니다.")
	String description,

	@NotNull(message = "메뉴 가격은 필수입니다.")
	int price,

	@NotNull(message = "메뉴 상태는 필수입니다.")
	MenuStatus status,

	@NotNull(message = "이미지 삭제 여부는 필수입니다.")
	Boolean isImageDeleted,

	List<Long> optionGroupIds
) {
}
