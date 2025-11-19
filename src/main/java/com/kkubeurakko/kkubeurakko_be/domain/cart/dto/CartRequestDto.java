package com.kkubeurakko.kkubeurakko_be.domain.cart.dto;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CartRequestDto(
	@NotNull(message = "메뉴 ID는 필수 입력값입니다.")
	Long menuId,

	@Min(value = 1, message = "수량은 최소 1개 이상이어야 합니다.")
	int quantity,

	@NotEmpty(message = "옵션 ID 목록은 비어 있을 수 없습니다.")
	List<@NotNull(message = "옵션 ID는 null일 수 없습니다.") Long> optionIdList
) {
}
