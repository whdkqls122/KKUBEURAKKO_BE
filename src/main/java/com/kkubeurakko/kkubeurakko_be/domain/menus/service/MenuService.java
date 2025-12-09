package com.kkubeurakko.kkubeurakko_be.domain.menus.service;

import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.AdminMenuDetailDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.AdminMenuUpdateDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuDetailResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuResponseDto;

public interface MenuService {
	List<MenuResponseDto> getAllMenuByMenuGroup(Long menuGroupId);

	MenuDetailResponseDto getMenuDetail(Long menuId);

	//메뉴 단건 조회 (관리자)
	AdminMenuDetailDto getMenuDetailForAdmin(Long menuId);

	//메뉴 수정 (관리자)
	void updateMenu(Long menuId, AdminMenuUpdateDto request);

	//메뉴 삭제 (관리자)
	void deleteMenu(Long menuId);
}
