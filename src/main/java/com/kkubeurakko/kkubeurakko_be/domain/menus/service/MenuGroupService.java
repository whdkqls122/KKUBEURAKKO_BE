package com.kkubeurakko.kkubeurakko_be.domain.menus.service;

import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuGroupRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuGroupResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuGroupUpdateDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuGroupWithMenusDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuStatus;

public interface MenuGroupService {
	List<MenuGroupResponseDto> getAllMenuGroupName();

	//메뉴 그룹 생성
	void createMenuGroup(MenuGroupRequestDto dto);

	//메뉴 그룹 정보 수정
	void updateMenuGroup(MenuGroupUpdateDto menuGroupUpdateDto);

	//메뉴 그룹 상태 변경
	void updateMenuGroupStatus(Long menuGroupId, MenuStatus status);

	//메뉴 그룹, 메뉴 전체 조회
	public List<MenuGroupWithMenusDto> getAllMenuGroupsWithMenus(Long storeId);
}
