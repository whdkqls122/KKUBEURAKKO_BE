package com.kkubeurakko.kkubeurakko_be.domain.menus.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuGroupResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuGroups;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuStatus;
import com.kkubeurakko.kkubeurakko_be.domain.menus.repository.MenuGroupRepository;

@ExtendWith(MockitoExtension.class)
class MenuGroupServiceImplTest {
	@Mock
	private MenuGroupRepository menuGroupRepository;

	@InjectMocks
	private MenuGroupServiceImpl menuGroupService;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
		menuGroupService = new MenuGroupServiceImpl(menuGroupRepository);
	}

	@Test
	@DisplayName("판매중인 메뉴 그룹들이 정상 조회 되는지 검증한다.")
	void getAllMenuGroupName() {
		MenuGroups group1 = MenuGroups.builder()
			.id(1L)
			.name("인기")
			.menuStatus(MenuStatus.ON_SALE)
			.build();

		MenuGroups group2 = MenuGroups.builder()
			.id(3L)
			.name("세트")
			.menuStatus(MenuStatus.ON_SALE)
			.build();

		MenuGroups group3 = MenuGroups.builder()
			.id(5L)
			.name("음료")
			.menuStatus(MenuStatus.ON_SALE)
			.build();
		List<MenuGroups> mockMenuGroups = Arrays.asList(group1, group2, group3);
		when(menuGroupRepository.findByStatus(MenuStatus.ON_SALE)).thenReturn(mockMenuGroups);
		//when
		List<MenuGroupResponseDto> result = menuGroupService.getAllMenuGroupName();

		// then
		assertThat(result).isNotNull();
		assertThat(result.size()).isEqualTo(3);
		assertThat(result.get(0).menuGroupId()).isEqualTo(1L);
		assertThat(result.get(0).name()).isEqualTo("인기");
		assertThat(result.get(1).menuGroupId()).isEqualTo(3L);
		assertThat(result.get(1).name()).isEqualTo("세트");
		assertThat(result.get(2).menuGroupId()).isEqualTo(5L);
		assertThat(result.get(2).name()).isEqualTo("음료");

		// verify
		verify(menuGroupRepository, times(1)).findByStatus(MenuStatus.ON_SALE);
	}

	@Test
	@DisplayName("조회된 메뉴 그룹이 없으면 예외가 발생하는지 검증한다.")
	void getAllMenuGroupNameEmpty() {
		// given
		when(menuGroupRepository.findByStatus(MenuStatus.ON_SALE)).thenReturn(List.of());

		assertThatThrownBy(() -> menuGroupService.getAllMenuGroupName())
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("메뉴 그룹이 조회되지 않았습니다.");

		verify(menuGroupRepository, times(1)).findByStatus(MenuStatus.ON_SALE);
	}
}