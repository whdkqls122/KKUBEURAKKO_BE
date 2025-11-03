package com.kkubeurakko.kkubeurakko_be.domain.menus.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuDetailResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.Menus;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.Status;
import com.kkubeurakko.kkubeurakko_be.domain.menus.repository.MenuRepository;
import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionGroupResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionResponseDto;

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

	@Override
	@Transactional(readOnly = true)
	public MenuDetailResponseDto getMenuDetail(Long menuId) {
		Menus menu = menuRepository.findMenuDetail(menuId)
			.orElseThrow(() -> new NullPointerException("조회된 메뉴가 없습니다."));

		return toResponseDto(menu);
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

	private MenuDetailResponseDto toResponseDto(Menus menu) {
		List<OptionGroupResponseDto> optionGroupResponseDtoList = menu.getOptionGroupList().stream()
			.filter(og -> og.getStatus() == com.kkubeurakko.kkubeurakko_be.domain.options.entity.Status.ON_SALE)
			.map(og -> OptionGroupResponseDto.builder()
				.id(og.getId())
				.name(og.getName())
				.description(og.getDescription())
				.required(og.isRequired())
				.optionGroupType(og.getOptionGroupType())
				.selectedNum(og.getSelectedNum())
				.status(og.getStatus())
				.options(og.getOptions().stream()
					.filter(o -> o.getStatus() == com.kkubeurakko.kkubeurakko_be.domain.options.entity.Status.ON_SALE)
					.map(o -> OptionResponseDto.builder()
						.id(o.getId())
						.name(o.getName())
						.price(o.getPrice())
						.status(o.getStatus())
						.build())
					.toList())
				.build())
			.toList();

		return MenuDetailResponseDto.builder()
			.menuId(menu.getId())
			.name(menu.getName())
			.description(menu.getDescription())
			.price(menu.getPrice())
			.img(menu.getImg())
			.optionGroupResponseDtoList(optionGroupResponseDtoList)
			.build();
	}
}
