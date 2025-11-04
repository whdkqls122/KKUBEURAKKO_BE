package com.kkubeurakko.kkubeurakko_be.domain.menus.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuGroupResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuGroups;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuStatus;
import com.kkubeurakko.kkubeurakko_be.domain.menus.repository.MenuGroupRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuGroupServiceImpl implements MenuGroupService {
	private final MenuGroupRepository menuGroupRepository;

	@Override
	public List<MenuGroupResponseDto> getAllMenuGroupName() {
		List<MenuGroups> menuGroupList = menuGroupRepository.findByMenuStatus(MenuStatus.ON_SALE);
		if (menuGroupList.isEmpty()) {
			log.info("조회된 메뉴 그룹이 없음");
			throw new RuntimeException("메뉴 그룹이 조회되지 않았습니다.");
		}
		log.info("메뉴 그룹 조회 성공");
		return toResponseDto(menuGroupList);
	}

	private List<MenuGroupResponseDto> toResponseDto(List<MenuGroups> menuGroupList) {
		return menuGroupList.stream()
			.map(menuGroups -> MenuGroupResponseDto
				.builder()
				.menuGroupId(menuGroups.getId())
				.name(menuGroups.getName())
				.build()
			)
			.collect(Collectors.toList());
	}
}
