package com.kkubeurakko.kkubeurakko_be.domain.menus.service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.AdminMenuDetailDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.AdminMenuRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.AdminMenuUpdateDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuDetailResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuGroups;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuStatus;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.Menus;
import com.kkubeurakko.kkubeurakko_be.domain.menus.repository.MenuGroupRepository;
import com.kkubeurakko.kkubeurakko_be.domain.menus.repository.MenuRepository;
import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionGroupResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroups;
import com.kkubeurakko.kkubeurakko_be.domain.options.repository.OptionGroupRepository;
import com.kkubeurakko.kkubeurakko_be.global.aws.dto.S3UploadResponseDto;
import com.kkubeurakko.kkubeurakko_be.global.aws.service.S3Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuServiceImpl implements MenuService {
	private final MenuRepository menuRepository;
	private final OptionGroupRepository optionGroupRepository;
	private final MenuGroupRepository menuGroupRepository;
	private final S3Service s3Service;

	@Override
	@Cacheable(
		value = "menuGroupMenus",
		key = "#menuGroupId"
	)
	public List<MenuResponseDto> getAllMenuByMenuGroup(Long menuGroupId) {
		List<Menus> menuList = menuRepository.findByMenuGroupIdAndMenuStatus(menuGroupId, MenuStatus.ON_SALE);
		if (menuList.isEmpty()) {
			log.info("조회된 메뉴가 없음");
			throw new IllegalArgumentException("메뉴가 조회되지 않았습니다.");
		}
		return toResponseDto(menuList);
	}

	@Override
	@Cacheable(
		value = "menuDetail",
		key = "#menuId"
	)
	@Transactional(readOnly = true)
	public MenuDetailResponseDto getMenuDetail(Long menuId) {
		Menus menu = menuRepository.findById(menuId)
			.orElseThrow(() -> new IllegalArgumentException("조회된 메뉴가 없습니다."));

		List<OptionGroups> optionGroups = optionGroupRepository.findAllByMenuId(menuId);

		return toResponseDto(menu, optionGroups);
	}

	@Override
	@Transactional(readOnly = true)
	public AdminMenuDetailDto getMenuDetailForAdmin(Long menuId) {

		Menus menu = menuRepository.findById(menuId)
			.orElseThrow(() -> new EntityNotFoundException("메뉴가 존재하지 않습니다."));

		List<OptionGroups> optionGroups = optionGroupRepository.findAllByMenuId(menuId);

		return adminMenuDetailDto(menu, optionGroups);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "menuGroupMenus", key = "#request.menuGroupId()"),
		@CacheEvict(value = "storeMenus", allEntries = true)
	})
	public void createMenu(AdminMenuRequestDto request, MultipartFile image) throws IOException {

		MenuGroups menuGroup = menuGroupRepository.findById(request.menuGroupId())
			.orElseThrow(() -> new IllegalArgumentException("메뉴 그룹이 존재하지 않습니다."));

		S3UploadResponseDto responseDto = null;
		if (image != null && !image.isEmpty()) {
			responseDto = s3Service.uploadImage(image);
		}
		Menus menu = Menus.builder()
			.name(request.name())
			.description(request.description())
			.price(request.price())
			.menuStatus(request.status())
			.img(responseDto != null ? responseDto.imageUrl() : null)
			.s3Key(responseDto != null ? responseDto.imageKey() : null)
			.menuGroup(menuGroup)
			.build();

		Menus savedMenu = menuRepository.save(menu);

		if (request.optionGroupIds() == null || request.optionGroupIds().isEmpty()) {
			return;
		}

		List<OptionGroups> optionGroups =
			optionGroupRepository.findAllById(request.optionGroupIds());

		optionGroups.forEach(savedMenu::addOptionGroup);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "menuDetail", key = "#menuId"),
		@CacheEvict(value = "menuGroupMenus", allEntries = true),
		@CacheEvict(value = "storeMenus", allEntries = true)
	})
	public void updateMenu(Long menuId, AdminMenuUpdateDto request, MultipartFile image) throws IOException {

		Menus menu = menuRepository.findById(menuId)
			.orElseThrow(() -> new EntityNotFoundException("메뉴가 존재하지 않습니다."));

		String imageUrl = menu.getImg();
		String s3Key = menu.getS3Key();

		boolean hasNewImage = image != null && !image.isEmpty();
		boolean deleteImage = request.isImageDeleted();

		if (deleteImage) {
			if (s3Key != null) {
				s3Service.deleteImage(s3Key);
			}
			imageUrl = null;
			s3Key = null;
		} else if (hasNewImage) {
			if (s3Key != null) {
				s3Service.deleteImage(s3Key);
			}

			S3UploadResponseDto upload = s3Service.uploadImage(image);
			imageUrl = upload.imageUrl();
			s3Key = upload.imageKey();
		}

		menu.update(
			request.name(),
			request.description(),
			request.price(),
			request.status(),
			imageUrl,
			s3Key
		);

		menu.clearOptionGroups();

		List<Long> optionGroupIds = request.optionGroupIds();
		if (optionGroupIds == null || optionGroupIds.isEmpty())
			return;

		List<OptionGroups> optionGroups =
			optionGroupRepository.findAllById(optionGroupIds);

		optionGroups.forEach(menu::addOptionGroup);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "menuDetail", key = "#menuId"),
		@CacheEvict(value = "menuGroupMenus", allEntries = true),
		@CacheEvict(value = "storeMenus", allEntries = true)
	})
	public void changeMenuStatus(Long menuId, MenuStatus status) {

		Menus menu = menuRepository.findById(menuId)
			.orElseThrow(() -> new IllegalArgumentException("메뉴가 존재하지 않습니다."));

		menu.changeStatus(status);
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

	private AdminMenuDetailDto adminMenuDetailDto(Menus menu, List<OptionGroups> optionGroups) {

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
