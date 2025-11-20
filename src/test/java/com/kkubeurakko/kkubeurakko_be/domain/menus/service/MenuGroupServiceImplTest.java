package com.kkubeurakko.kkubeurakko_be.domain.menus.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
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

import com.kkubeurakko.kkubeurakko_be.TestFixture;
import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuGroupRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuGroupResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuGroupUpdateDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuGroups;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuStatus;
import com.kkubeurakko.kkubeurakko_be.domain.menus.repository.MenuGroupRepository;
import com.kkubeurakko.kkubeurakko_be.domain.menus.repository.MenuRepository;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.StoreRepository;

@ExtendWith(MockitoExtension.class)
class MenuGroupServiceImplTest {
	@Mock
	private MenuGroupRepository menuGroupRepository;

	@Mock
	private StoreRepository storeRepository;

	@Mock
	private MenuRepository menuRepository;

	@InjectMocks
	private MenuGroupServiceImpl menuGroupService;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
		menuGroupService = new MenuGroupServiceImpl(menuGroupRepository, storeRepository, menuRepository);
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
		when(menuGroupRepository.findByMenuStatus(MenuStatus.ON_SALE)).thenReturn(mockMenuGroups);
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
		verify(menuGroupRepository, times(1)).findByMenuStatus(MenuStatus.ON_SALE);
	}

	@Test
	@DisplayName("조회된 메뉴 그룹이 없으면 예외가 발생하는지 검증한다.")
	void getAllMenuGroupNameEmpty() {
		// given
		when(menuGroupRepository.findByMenuStatus(MenuStatus.ON_SALE)).thenReturn(List.of());

		assertThatThrownBy(() -> menuGroupService.getAllMenuGroupName())
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("메뉴 그룹이 조회되지 않았습니다.");

		verify(menuGroupRepository, times(1)).findByMenuStatus(MenuStatus.ON_SALE);
	}

	@Test
	@DisplayName("메뉴 그룹 생성이 정상적으로 수행되는지 검증한다.")
	void createMenuGroup_ShouldCreateMenuGroupSuccessfully() {
		// given
		MenuGroupRequestDto dto = new MenuGroupRequestDto("인기메뉴", "꾸브라꼬 대표 인기 메뉴");
		Stores mockStore = Stores.builder()
			.id(1L)
			.address("경기도 안양시 동안구 관양로 123")
			.tel("031-111-2222")
			.isOpen(true)
			.minOrderPrice(12000)
			.delivery_region("안양 전지역")
			.build();

		when(storeRepository.findById(1L)).thenReturn(Optional.of(mockStore));
		when(menuGroupRepository.save(any(MenuGroups.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// when
		menuGroupService.createMenuGroup(dto);

		// then
		verify(storeRepository, times(1)).findById(1L);
		verify(menuGroupRepository, times(1)).save(any(MenuGroups.class));
	}

	@Test
	@DisplayName("메뉴 그룹 수정이 정상적으로 수행하는지 검증한다.")
	void updateMenuGroup_Success() {
		// given
		Long menuGroupId = 1L;
		MenuGroupUpdateDto dto = new MenuGroupUpdateDto(menuGroupId, "수정된 이름", "수정된 설명");
		Stores store = TestFixture.createStore();

		MenuGroups existingMenuGroup = MenuGroups.builder()
			.id(menuGroupId)
			.name("기존 이름")
			.description("기존 설명")
			.menuStatus(MenuStatus.ON_SALE)
			.store(store)
			.build();

		when(menuGroupRepository.findById(menuGroupId)).thenReturn(Optional.of(existingMenuGroup));

		// when
		menuGroupService.updateMenuGroup(dto);

		// then
		assertEquals("수정된 이름", existingMenuGroup.getName());
		assertEquals("수정된 설명", existingMenuGroup.getDescription());
		verify(menuGroupRepository, times(1)).findById(menuGroupId);
	}

	@Test
	@DisplayName("메뉴 그룹 상태 변경이 정상적으로 수행하는지 검증한다.")
	void changeMenuGroupStatus_ShouldUpdateStatus() {
		// given
		Stores store = TestFixture.createStore();

		MenuGroups menuGroup = MenuGroups.builder()
			.id(1L)
			.name("인기 메뉴")
			.description("꾸브라꼬 인기 메뉴 모음")
			.menuStatus(MenuStatus.ON_SALE)
			.store(store)
			.build();

		when(menuGroupRepository.findById(1L)).thenReturn(Optional.of(menuGroup));

		// when
		menuGroupService.updateMenuGroupStatus(1L, MenuStatus.HIDE);

		// then
		assertThat(menuGroup.getMenuStatus()).isEqualTo(MenuStatus.HIDE);
		verify(menuGroupRepository, times(1)).findById(1L);
	}

}