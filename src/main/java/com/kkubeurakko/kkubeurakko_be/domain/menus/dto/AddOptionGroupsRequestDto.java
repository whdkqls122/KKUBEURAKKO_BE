package com.kkubeurakko.kkubeurakko_be.domain.menus.dto;

import java.util.List;

public record AddOptionGroupsRequestDto(
	List<Long> optionGroupIds
) {
}
