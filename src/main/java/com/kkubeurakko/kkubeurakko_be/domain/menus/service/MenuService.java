package com.kkubeurakko.kkubeurakko_be.domain.menus.service;

import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.AdminMenuDetailDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.AdminMenuUpdateDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuDetailResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuResponseDto;

public interface MenuService {
	List<MenuResponseDto> getAllMenuByMenuGroup(Long menuGroupId);

	MenuDetailResponseDto getMenuDetail(Long menuId);

	//메뉴에 옵션 그룹 추가/갱신
	void updateOptionGroups(Long menuId, List<Long> optionGroupIds);

	//메뉴 단건 조회 (관리자)
	AdminMenuDetailDto getMenuDetailForAdmin(Long menuId);

	//메뉴 수정 (관리자)
	void updateMenu(Long menuId, AdminMenuUpdateDto request);
}
