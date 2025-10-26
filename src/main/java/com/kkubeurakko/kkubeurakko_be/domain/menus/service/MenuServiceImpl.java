package com.kkubeurakko.kkubeurakko_be.domain.menus.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.Menus;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.Status;
import com.kkubeurakko.kkubeurakko_be.domain.menus.repository.MenuRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuServiceImpl implements MenuService {
	private final MenuRepository menuRepository;

	@Override
	public List<MenuResponseDto> getAllMenuByMenuGroup(Long menuGroupId) {
		List<Menus> menuList = menuRepository.findByMenuGroupIdAndStatus(menuGroupId, Status.ON_SALE);
		if (menuList.isEmpty()) {
			log.info("조회된 메뉴가 없음");
			throw new RuntimeException("메뉴가 조회되지 않았습니다.");
		}
		return toResponseDto(menuList);
	}

	private List<MenuResponseDto> toResponseDto(List<Menus> menuList) {
		return menuList.stream()
			.map(menus -> MenuResponseDto
				.builder()
				.menu_id(menus.getId())
				.description(menus.getDescription())
				.name(menus.getName())
				.img(menus.getImg())
				.price(menus.getPrice())
				.build())
			.collect(Collectors.toList());
	}
}
