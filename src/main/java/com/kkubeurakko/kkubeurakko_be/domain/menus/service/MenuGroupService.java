package com.kkubeurakko.kkubeurakko_be.domain.menus.service;

import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuGroupResponseDto;

public interface MenuGroupService {
	List<MenuGroupResponseDto> getAllMenuGroupName();
}
