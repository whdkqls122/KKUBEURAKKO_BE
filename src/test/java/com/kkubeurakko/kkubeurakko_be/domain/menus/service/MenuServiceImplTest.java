package com.kkubeurakko.kkubeurakko_be.domain.menus.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuDetailResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.Menus;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.Status;
import com.kkubeurakko.kkubeurakko_be.domain.menus.repository.MenuRepository;
import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionGroupResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroupType;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroups;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.Options;

@ExtendWith(MockitoExtension.class)
class MenuServiceImplTest {

	@Mock
	private MenuRepository menuRepository;

	@InjectMocks
	private MenuServiceImpl menuService;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
		menuService = new MenuServiceImpl(menuRepository);
	}

	@Test
	@DisplayName("메뉴 그룹 별 메뉴 조회가 정상적으로 되는지 검증한다.")
	void getAllMenuByMenuGroup() {
		// given
		Long menuGroupId = 1L;
		Menus menu1 = Menus.builder()
			.id(1L)
			.name("양념꾸브")
			.description("양념꾸브입니다.")
			.price(18000)
			.img("example1.jpg")
			.status(Status.ON_SALE)
			.build();

		Menus menu2 = Menus.builder()
			.id(2L)
			.name("소금꾸브")
			.description("소금꾸브입니다.")
			.price(19000)
			.img("example2.jpg")
			.status(Status.ON_SALE)
			.build();

		List<Menus> mockMenus = Arrays.asList(menu1, menu2);

		when(menuRepository.findByMenuGroupIdAndStatus(menuGroupId, Status.ON_SALE))
			.thenReturn(mockMenus);

		// when
		List<MenuResponseDto> result = menuService.getAllMenuByMenuGroup(menuGroupId);

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).menu_id()).isEqualTo(1L);
		assertThat(result.get(0).name()).isEqualTo("양념꾸브");
		assertThat(result.get(0).description()).isEqualTo("양념꾸브입니다.");
		assertThat(result.get(0).price()).isEqualTo(18000);

		assertThat(result.get(1).menu_id()).isEqualTo(2L);
		assertThat(result.get(1).name()).isEqualTo("소금꾸브");
		assertThat(result.get(1).description()).isEqualTo("소금꾸브입니다.");
		assertThat(result.get(1).price()).isEqualTo(19000);

		verify(menuRepository, times(1)).findByMenuGroupIdAndStatus(menuGroupId, Status.ON_SALE);
	}

	@Test
	@DisplayName("메뉴 그룹에 메뉴가 없으면 예외가 발생하는지 검증한다.")
	void getAllMenuByMenuGroup_empty_throws() {
		// given
		Long menuGroupId = 1L;
		when(menuRepository.findByMenuGroupIdAndStatus(menuGroupId, Status.ON_SALE))
			.thenReturn(Arrays.asList());
		// when & then
		assertThatThrownBy(() -> menuService.getAllMenuByMenuGroup(menuGroupId))
			.isInstanceOf(RuntimeException.class)
			.hasMessage("메뉴가 조회되지 않았습니다.");

		verify(menuRepository, times(1)).findByMenuGroupIdAndStatus(menuGroupId, Status.ON_SALE);
	}

	@Test
	@DisplayName("메뉴 상세조회가 정상적으로 이루어지는지 검증한다.")
	void getMenuDetail() {
		// Option 엔티티
		Options option1 = Options.builder()
			.id(1L)
			.name("순한맛")
			.price(1000)
			.status(com.kkubeurakko.kkubeurakko_be.domain.options.entity.Status.ON_SALE)
			.build();

		Options option2 = Options.builder()
			.id(2L)
			.name("중간맛")
			.price(500)
			.status(com.kkubeurakko.kkubeurakko_be.domain.options.entity.Status.ON_SALE)
			.build();

		Options option3 = Options.builder()
			.id(2L)
			.name("매운맛")
			.price(500)
			.status(com.kkubeurakko.kkubeurakko_be.domain.options.entity.Status.ON_SALE)
			.build();

		// OptionGroup 엔티티
		OptionGroups optionGroup = OptionGroups.builder()
			.id(10L)
			.name("맵기 선택")
			.description("맵기 선택")
			.required(true)
			.selectedNum(1)
			.optionGroupType(OptionGroupType.SINGLE)
			.status(com.kkubeurakko.kkubeurakko_be.domain.options.entity.Status.ON_SALE)
			.options(List.of(option1, option2, option3))
			.build();

		// Menu 엔티티
		Menus menu = Menus.builder()
			.id(1L)
			.name("양념꾸브")
			.description("양념꾸브입니다.")
			.price(18000)
			.img("example1.jpg")
			.status(Status.ON_SALE)
			.optionGroupList(List.of(optionGroup))
			.build();

		// given
		when(menuRepository.findMenuDetail(1L)).thenReturn(Optional.of(menu));

		// when
		MenuDetailResponseDto result = menuService.getMenuDetail(1L);

		// then
		verify(menuRepository, times(1)).findMenuDetail(1L);
		assertThat(result.menuId()).isEqualTo(1L);
		assertThat(result.name()).isEqualTo("양념꾸브");
		assertThat(result.optionGroupResponseDtoList()).hasSize(1);

		OptionGroupResponseDto optionGroupResponseDto = result.optionGroupResponseDtoList().get(0);
		assertThat(optionGroupResponseDto.name()).isEqualTo("맵기 선택");
		assertThat(optionGroupResponseDto.options()).hasSize(3);
		assertThat(optionGroupResponseDto.options().get(0).name()).isEqualTo("순한맛");
		assertThat(optionGroupResponseDto.options().get(1).name()).isEqualTo("중간맛");
		assertThat(optionGroupResponseDto.options().get(2).name()).isEqualTo("매운맛");
	}

	@Test
	@DisplayName("존재하지 않는 메뉴 상세조회 시 예외가 발생하는지 검증한다.")
	void getMenuDetail_notFound_throws() {
		// given
		Long menuId = 999L;
		when(menuRepository.findMenuDetail(menuId))
			.thenReturn(java.util.Optional.empty());

		// when & then
		assertThatThrownBy(() -> menuService.getMenuDetail(menuId))
			.isInstanceOf(RuntimeException.class)
			.hasMessage("조회된 메뉴가 없습니다.");

		verify(menuRepository, times(1)).findMenuDetail(menuId);
	}
}