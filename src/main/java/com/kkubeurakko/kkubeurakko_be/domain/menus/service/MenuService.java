package com.kkubeurakko.kkubeurakko_be.domain.menus.service;

import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuResponseDto;

public interface MenuService {
	List<MenuResponseDto> getAllMenuByMenuGroup(Long menuGroupId);
}
