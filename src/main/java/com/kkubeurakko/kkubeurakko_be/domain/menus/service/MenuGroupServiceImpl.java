package com.kkubeurakko.kkubeurakko_be.domain.menus.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuGroupRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuGroupResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuGroupUpdateDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuGroupWithMenusDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuSummaryDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuGroups;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuStatus;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.Menus;
import com.kkubeurakko.kkubeurakko_be.domain.menus.repository.MenuGroupRepository;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.StoreRepository;
import com.kkubeurakko.kkubeurakko_be.global.aws.service.S3Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuGroupServiceImpl implements MenuGroupService {

	private final MenuGroupRepository menuGroupRepository;
	private final StoreRepository storeRepository;
	private final S3Service s3Service;

	@Override
	@Cacheable("menuGroups")
	public List<MenuGroupResponseDto> getAllMenuGroupName() {
		List<MenuGroups> menuGroupList = menuGroupRepository.findByMenuStatus(MenuStatus.ON_SALE);
		if (menuGroupList.isEmpty()) {
			log.info("조회된 메뉴 그룹이 없음");
			throw new EntityNotFoundException("메뉴 그룹이 조회되지 않았습니다.");
		}
		log.info("메뉴 그룹 조회 성공");
		return toResponseDto(menuGroupList);
	}

	@Override
	@Caching(evict = {
		@CacheEvict(value = "menuGroups", allEntries = true),
		@CacheEvict(value = "storeMenus", allEntries = true),
		@CacheEvict(value = "menuGroupMenus", allEntries = true)
	})
	public void createMenuGroup(MenuGroupRequestDto dto) {
		Stores store = storeRepository.findById(1L)
			.orElseThrow(() -> new IllegalArgumentException("해당 매장을 찾을 수 없습니다."));

		MenuGroups menuGroup = MenuGroups.builder()
			.name(dto.name())
			.description(dto.description())
			.active(true)
			.menuStatus(MenuStatus.ON_SALE)
			.store(store)
			.build();

		menuGroupRepository.save(menuGroup);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "menuGroups", allEntries = true),
		@CacheEvict(value = "storeMenus", allEntries = true),
		@CacheEvict(value = "menuGroupMenus", allEntries = true)
	})
	public void updateMenuGroup(MenuGroupUpdateDto menuGroupUpdateDto) {
		MenuGroups menuGroup = menuGroupRepository.findById(menuGroupUpdateDto.menuGroupId())
			.orElseThrow(() -> new EntityNotFoundException("존재하지 않는 메뉴 그룹입니다."));

		menuGroup.updateMenuGroup(menuGroupUpdateDto.name(), menuGroupUpdateDto.description());
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "menuGroups", allEntries = true),
		@CacheEvict(value = "storeMenus", allEntries = true),
		@CacheEvict(value = "menuGroupMenus", allEntries = true)
	})
	public void updateMenuGroupStatus(Long menuGroupId, MenuStatus status) {
		MenuGroups menuGroup = menuGroupRepository.findById(menuGroupId)
			.orElseThrow(() -> new EntityNotFoundException("존재하지 않는 메뉴 그룹입니다."));

		menuGroup.updateStatus(status);
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(
		value = "storeMenus",
		key = "#storeId"
	)
	public List<MenuGroupWithMenusDto> getAllMenuGroupsWithMenus(Long storeId) {
		List<MenuGroups> groups = menuGroupRepository.findAllByStoreIdWithMenus(storeId);

		return groups.stream()
			.map(this::toMenuGroupWithMenusDto)
			.collect(Collectors.toList());
	}

	@Override
	@Caching(evict = {
		@CacheEvict(value = "menuGroups", allEntries = true),
		@CacheEvict(value = "storeMenus", allEntries = true),
		@CacheEvict(value = "menuGroupMenus", allEntries = true)
	})
	public void deleteMenuGroup(Long menuGroupId) {
		MenuGroups menuGroup = menuGroupRepository.findById(menuGroupId)
			.orElseThrow(() -> new EntityNotFoundException("메뉴 그룹이 존재하지 않습니다."));

		for (Menus menu : new ArrayList<>(menuGroup.getMenus())) {
			if (menu.getS3Key() != null) {
				s3Service.deleteImage(menu.getS3Key());
			}
			menuGroup.removeMenu(menu);
		}

		menuGroup.deleteMenuGroup();
		menuGroup.getMenus().forEach(Menus::deleteMenu);
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

	private MenuGroupWithMenusDto toMenuGroupWithMenusDto(MenuGroups group) {

		List<Menus> menus = group.getMenus() != null ? group.getMenus() : new ArrayList<>();

		return MenuGroupWithMenusDto.builder()
			.id(group.getId())
			.name(group.getName())
			.description(group.getDescription())
			.menuStatus(group.getMenuStatus())
			.menus(
				menus.stream()
					.filter(Menus::isActive)
					.map(this::toMenuSummaryDto)
					.collect(Collectors.toList())
			)
			.build();
	}

	private MenuSummaryDto toMenuSummaryDto(Menus menu) {
		return MenuSummaryDto.builder()
			.id(menu.getId())
			.name(menu.getName())
			.description(menu.getDescription())
			.price(menu.getPrice())
			.img(menu.getImg())
			.menuStatus(menu.getMenuStatus())
			.build();
	}
}
