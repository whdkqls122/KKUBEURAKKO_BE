package com.kkubeurakko.kkubeurakko_be.domain.menus.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.AdminMenuDetailDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.AdminMenuUpdateDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuDetailResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuStatus;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.Menus;
import com.kkubeurakko.kkubeurakko_be.domain.menus.repository.MenuRepository;
import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionGroupResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroups;
import com.kkubeurakko.kkubeurakko_be.domain.options.repository.OptionGroupRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuServiceImpl implements MenuService {
	private final MenuRepository menuRepository;
	private final OptionGroupRepository optionGroupRepository;

	@Override
	public List<MenuResponseDto> getAllMenuByMenuGroup(Long menuGroupId) {
		List<Menus> menuList = menuRepository.findByMenuGroupIdAndMenuStatus(menuGroupId, MenuStatus.ON_SALE);
		if (menuList.isEmpty()) {
			log.info("조회된 메뉴가 없음");
			throw new IllegalArgumentException("메뉴가 조회되지 않았습니다.");
		}
		return toResponseDto(menuList);
	}

	@Override
	@Transactional(readOnly = true)
	public MenuDetailResponseDto getMenuDetail(Long menuId) {
		Menus menu = menuRepository.findById(menuId)
			.orElseThrow(() -> new IllegalArgumentException("조회된 메뉴가 없습니다."));

		List<OptionGroups> optionGroups = optionGroupRepository.findAllByMenuId(menuId);

		return toResponseDto(menu, optionGroups);
	}

	@Override
	@Transactional
	public void updateOptionGroups(Long menuId, List<Long> optionGroupIds) {
		Menus menu = menuRepository.findById(menuId)
			.orElseThrow(() -> new EntityNotFoundException("메뉴가 존재하지 않습니다."));

		menu.clearOptionGroups();

		List<OptionGroups> optionGroups = optionGroupRepository.findAllById(optionGroupIds);
		optionGroups.forEach(menu::addOptionGroup);
	}

	@Override
	@Transactional(readOnly = true)
	public AdminMenuDetailDto getMenuDetailForAdmin(Long menuId) {

		Menus menu = menuRepository.findById(menuId)
			.orElseThrow(() -> new EntityNotFoundException("메뉴가 존재하지 않습니다."));

		List<OptionGroups> optionGroups = optionGroupRepository.findAllByMenuId(menuId);

		return AdminMenuDetailDto(menu, optionGroups);
	}

	@Override
	public void updateMenu(Long menuId, AdminMenuUpdateDto request) {

		Menus menu = menuRepository.findById(menuId)
			.orElseThrow(() -> new EntityNotFoundException("메뉴가 존재하지 않습니다."));

		menu.update(
			request.name(),
			request.description(),
			request.price(),
			request.status(),
			request.img()
		);

		menu.clearOptionGroups();

		List<Long> optionGroupIds = request.optionGroupIds();
		if (optionGroupIds == null || optionGroupIds.isEmpty())
			return;

		List<OptionGroups> optionGroups =
			optionGroupRepository.findAllById(optionGroupIds);

		optionGroups.forEach(menu::addOptionGroup);
	}

	private List<MenuResponseDto> toResponseDto(List<Menus> menuList) {
		return menuList.stream()
			.map(menus -> MenuResponseDto
				.builder()
				.menuId(menus.getId())
				.description(menus.getDescription())
				.name(menus.getName())
				.img(menus.getImg())
				.price(menus.getPrice())
				.build())
			.collect(Collectors.toList());
	}

	private MenuDetailResponseDto toResponseDto(Menus menu, List<OptionGroups> optionGroups) {
		List<OptionGroupResponseDto> optionGroupResponseDtoList = toOptionGroupDtoList(optionGroups);

		return MenuDetailResponseDto.builder()
			.menuId(menu.getId())
			.name(menu.getName())
			.description(menu.getDescription())
			.price(menu.getPrice())
			.img(menu.getImg())
			.optionGroupResponseDtoList(optionGroupResponseDtoList)
			.build();
	}

	private List<OptionGroupResponseDto> toOptionGroupDtoList(List<OptionGroups> optionGroups) {
		return optionGroups.stream()
			.map(og -> OptionGroupResponseDto.builder()
				.id(og.getId())
				.name(og.getName())
				.description(og.getDescription())
				.required(og.isRequired())
				.optionGroupType(og.getOptionGroupType())
				.selectedNum(og.getSelectedNum())
				.optionStatus(og.getOptionStatus())
				.options(
					og.getOptions().stream()
						.map(o -> OptionResponseDto.builder()
							.id(o.getId())
							.name(o.getName())
							.price(o.getPrice())
							.optionStatus(o.getOptionStatus())
							.build())
						.toList()
				)
				.build())
			.toList();
	}

	private AdminMenuDetailDto AdminMenuDetailDto(Menus menu, List<OptionGroups> optionGroups) {

		List<AdminMenuDetailDto.OptionGroupSimpleDto> optionGroupDtos =
			optionGroups.stream()
				.map(og -> AdminMenuDetailDto.OptionGroupSimpleDto.builder()
					.id(og.getId())
					.name(og.getName())
					.build()
				)
				.toList();

		return AdminMenuDetailDto.builder()
			.menuId(menu.getId())
			.name(menu.getName())
			.description(menu.getDescription())
			.price(menu.getPrice())
			.status(menu.getMenuStatus())
			.img(menu.getImg())
			.optionGroups(optionGroupDtos)
			.build();
	}
}
