package com.kkubeurakko.kkubeurakko_be.domain.menus.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.kkubeurakko.kkubeurakko_be.TestFixture;
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
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroupType;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroups;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionStatus;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.Options;
import com.kkubeurakko.kkubeurakko_be.domain.options.repository.OptionGroupRepository;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;
import com.kkubeurakko.kkubeurakko_be.global.aws.dto.S3UploadResponseDto;
import com.kkubeurakko.kkubeurakko_be.global.aws.service.S3Service;

@ExtendWith(MockitoExtension.class)
class MenuServiceImplTest {

	@Mock
	private MenuRepository menuRepository;

	@Mock
	private OptionGroupRepository optionGroupRepository;

	@Mock
	private MenuGroupRepository menuGroupRepository;

	@InjectMocks
	private MenuServiceImpl menuService;

	@Mock
	private S3Service s3Service;

	// @BeforeEach
	// void setup() {
	// 	MockitoAnnotations.openMocks(this);
	// }

	private Stores store;
	private MenuGroups group;
	private Menus menu;
	private OptionGroups optionGroup;
	private Options option1;
	private Options option2;

	@BeforeEach
	void setUp() {
		store = TestFixture.createStore();
		group = TestFixture.createMenuGroup(1L, store);
		menu = TestFixture.createMenu(1L, group);

		optionGroup = TestFixture.createOptionGroup(1L);
		option1 = TestFixture.createOption(1L, "순한맛", 0, optionGroup);
		option2 = TestFixture.createOption(2L, "매운맛", 500, optionGroup);
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
			.menuStatus(MenuStatus.ON_SALE)
			.build();

		Menus menu2 = Menus.builder()
			.id(2L)
			.name("소금꾸브")
			.description("소금꾸브입니다.")
			.price(19000)
			.img("example2.jpg")
			.menuStatus(MenuStatus.ON_SALE)
			.build();

		List<Menus> mockMenus = Arrays.asList(menu1, menu2);

		when(menuRepository.findByMenuGroupIdAndMenuStatus(menuGroupId, MenuStatus.ON_SALE))
			.thenReturn(mockMenus);

		// when
		List<MenuResponseDto> result = menuService.getAllMenuByMenuGroup(menuGroupId);

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).menuId()).isEqualTo(1L);
		assertThat(result.get(0).name()).isEqualTo("양념꾸브");
		assertThat(result.get(0).description()).isEqualTo("양념꾸브입니다.");
		assertThat(result.get(0).price()).isEqualTo(18000);

		assertThat(result.get(1).menuId()).isEqualTo(2L);
		assertThat(result.get(1).name()).isEqualTo("소금꾸브");
		assertThat(result.get(1).description()).isEqualTo("소금꾸브입니다.");
		assertThat(result.get(1).price()).isEqualTo(19000);

		verify(menuRepository, times(1)).findByMenuGroupIdAndMenuStatus(menuGroupId, MenuStatus.ON_SALE);
	}

	@Test
	@DisplayName("메뉴 그룹에 메뉴가 없으면 예외가 발생하는지 검증한다.")
	void getAllMenuByMenuGroup_empty_throws() {
		// given
		Long menuGroupId = 1L;
		when(menuRepository.findByMenuGroupIdAndMenuStatus(menuGroupId, MenuStatus.ON_SALE))
			.thenReturn(List.of());
		// when & then
		assertThatThrownBy(() -> menuService.getAllMenuByMenuGroup(menuGroupId))
			.isInstanceOf(RuntimeException.class)
			.hasMessage("메뉴가 조회되지 않았습니다.");

		verify(menuRepository, times(1)).findByMenuGroupIdAndMenuStatus(menuGroupId, MenuStatus.ON_SALE);
	}

	@Test
	@DisplayName("메뉴 상세조회가 정상적으로 이루어지는지 검증한다.")
	void getMenuDetail() {
		// Option 엔티티
		Options option1 = Options.builder()
			.id(1L)
			.name("순한맛")
			.price(1000)
			.optionStatus(OptionStatus.ON_SALE)
			.build();

		Options option2 = Options.builder()
			.id(2L)
			.name("중간맛")
			.price(500)
			.optionStatus(OptionStatus.ON_SALE)
			.build();

		Options option3 = Options.builder()
			.id(2L)
			.name("매운맛")
			.price(500)
			.optionStatus(OptionStatus.ON_SALE)
			.build();

		// OptionGroup 엔티티
		OptionGroups optionGroup = OptionGroups.builder()
			.id(10L)
			.name("맵기 선택")
			.description("맵기 선택")
			.required(true)
			.selectedNum(1)
			.optionGroupType(OptionGroupType.SINGLE)
			.optionStatus(OptionStatus.ON_SALE)
			.options(List.of(option1, option2, option3))
			.build();

		// Menu 엔티티
		Menus menu = Menus.builder()
			.id(1L)
			.name("양념꾸브")
			.description("양념꾸브입니다.")
			.price(18000)
			.img("example1.jpg")
			.menuStatus(MenuStatus.ON_SALE)
			.build();

		List<OptionGroups> optionGroups = List.of(optionGroup);

		// given
		when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
		when(optionGroupRepository.findAllByMenuId(1L)).thenReturn(optionGroups);

		// when
		MenuDetailResponseDto result = menuService.getMenuDetail(1L);

		// then
		verify(menuRepository, times(1)).findById(1L);
		verify(optionGroupRepository, times(1)).findAllByMenuId(1L);

		assertThat(result.menuId()).isEqualTo(1L);
		assertThat(result.name()).isEqualTo("양념꾸브");
		assertThat(result.optionGroupResponseDtoList()).hasSize(1);

		OptionGroupResponseDto dto = result.optionGroupResponseDtoList().get(0);
		assertThat(dto.name()).isEqualTo("맵기 선택");
		assertThat(dto.options()).hasSize(3);
		assertThat(dto.options().get(0).name()).isEqualTo("순한맛");
		assertThat(dto.options().get(1).name()).isEqualTo("중간맛");
		assertThat(dto.options().get(2).name()).isEqualTo("매운맛");
	}

	@Test
	@DisplayName("존재하지 않는 메뉴 상세조회 시 예외가 발생하는지 검증한다.")
	void getMenuDetail_notFound_throws() {
		// given
		Long menuId = 999L;
		when(menuRepository.findById(menuId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> menuService.getMenuDetail(menuId))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("조회된 메뉴가 없습니다.");

		verify(menuRepository, times(1)).findById(menuId);
	}

	@Test
	@DisplayName("메뉴 상세 조회가 정상적으로 수행되는지 검증한다.")
	void getMenuDetail_success() {
		// given
		when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
		when(optionGroupRepository.findAllByMenuId(1L)).thenReturn(List.of(optionGroup));

		// when
		MenuDetailResponseDto dto = menuService.getMenuDetail(1L);

		// then
		assertThat(dto).isNotNull();
		assertThat(dto.name()).isEqualTo("양념꾸브");
		assertThat(dto.optionGroupResponseDtoList()).hasSize(1);
		assertThat(dto.optionGroupResponseDtoList().get(0).options()).hasSize(2);

		// verify
		verify(menuRepository, times(1)).findById(1L);
		verify(optionGroupRepository, times(1)).findAllByMenuId(1L);
	}

	@Test
	@DisplayName("관리자 메뉴 상세 조회가 정상적으로 이루어지는지 검증한다.")
	void getMenuDetailForAdmin_success() {
		// given
		when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
		when(optionGroupRepository.findAllByMenuId(1L)).thenReturn(List.of(optionGroup));

		// when
		AdminMenuDetailDto dto = menuService.getMenuDetailForAdmin(1L);

		// then
		assertThat(dto).isNotNull();
		assertThat(dto.menuId()).isEqualTo(1L);
		assertThat(dto.name()).isEqualTo(menu.getName());
		assertThat(dto.status()).isEqualTo(menu.getMenuStatus());
		assertThat(dto.optionGroups()).hasSize(1);
		assertThat(dto.optionGroups().get(0).name()).isEqualTo(optionGroup.getName());

		verify(menuRepository, times(1)).findById(1L);
		verify(optionGroupRepository, times(1)).findAllByMenuId(1L);
	}

	@Test
	@DisplayName("관리자 메뉴 수정이 정상적으로 이루어지는지 검증한다.")
	void updateMenu_success() throws IOException {
		// given
		Long menuId = 1L;

		Menus menu = Menus.builder()
			.id(menuId)
			.name("이전메뉴명")
			.description("이전설명")
			.price(10000)
			.menuStatus(MenuStatus.ON_SALE)
			.img("old.jpg")
			.s3Key("menu/old.jpg")
			.build();

		when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));

		AdminMenuUpdateDto request = AdminMenuUpdateDto.builder()
			.name("새로운메뉴명")
			.description("새로운설명")
			.price(15000)
			.status(MenuStatus.ON_SALE)
			.optionGroupIds(List.of(10L, 11L))
			.isImageDeleted(false)
			.build();

		MockMultipartFile image = new MockMultipartFile(
			"image",
			"new.jpg",
			"image/jpeg",
			"new-image".getBytes()
		);

		S3UploadResponseDto s3Response = S3UploadResponseDto.builder()
			.imageUrl("https://cloudfront/new.jpg")
			.imageKey("menu/new.jpg")
			.build();

		when(s3Service.uploadImage(any(MultipartFile.class)))
			.thenReturn(s3Response);

		OptionGroups og1 = OptionGroups.builder().id(10L).build();
		OptionGroups og2 = OptionGroups.builder().id(11L).build();

		when(optionGroupRepository.findAllById(List.of(10L, 11L)))
			.thenReturn(List.of(og1, og2));

		// when
		menuService.updateMenu(menuId, request, image);

		// then
		assertThat(menu.getName()).isEqualTo("새로운메뉴명");
		assertThat(menu.getDescription()).isEqualTo("새로운설명");
		assertThat(menu.getPrice()).isEqualTo(15000);
		assertThat(menu.getImg()).isEqualTo("https://cloudfront/new.jpg");
		assertThat(menu.getS3Key()).isEqualTo("menu/new.jpg");

		verify(s3Service, times(1)).uploadImage(any(MultipartFile.class));
		verify(s3Service, times(1)).deleteImage("menu/old.jpg");

		assertThat(menu.getMenuOptionGroups())
			.extracting(mog -> mog.getOptionGroup().getId())
			.containsExactlyInAnyOrder(10L, 11L);
	}

	@Test
	@DisplayName("관리자 메뉴 수정에서 이미지가 삭제되었더라도 정상적으로 이루어지는지 검증한다.")
	void updateMenu_success_image_deleted() throws IOException {
		// given
		Long menuId = 1L;

		Menus menu = Menus.builder()
			.id(menuId)
			.name("이전메뉴명")
			.description("이전설명")
			.price(10000)
			.menuStatus(MenuStatus.ON_SALE)
			.img("old.jpg")
			.s3Key("menu/old.jpg")
			.build();

		when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));

		AdminMenuUpdateDto request = AdminMenuUpdateDto.builder()
			.name("새로운메뉴명")
			.description("새로운설명")
			.price(15000)
			.status(MenuStatus.ON_SALE)
			.optionGroupIds(List.of(10L, 11L))
			.isImageDeleted(true)
			.build();

		OptionGroups og1 = OptionGroups.builder().id(10L).build();
		OptionGroups og2 = OptionGroups.builder().id(11L).build();

		when(optionGroupRepository.findAllById(List.of(10L, 11L)))
			.thenReturn(List.of(og1, og2));

		// when
		menuService.updateMenu(menuId, request, null);

		// then
		assertThat(menu.getName()).isEqualTo("새로운메뉴명");
		assertThat(menu.getDescription()).isEqualTo("새로운설명");
		assertThat(menu.getPrice()).isEqualTo(15000);

		assertThat(menu.getMenuOptionGroups())
			.extracting(mog -> mog.getOptionGroup().getId())
			.containsExactlyInAnyOrder(10L, 11L);
	}

	@Test
	@DisplayName("관리자 메뉴 생성이 정상적으로 이루어지는지 검증한다.")
	void createMenu() throws Exception {
		// given
		AdminMenuRequestDto request = AdminMenuRequestDto.builder()
			.name("새로운메뉴")
			.description("맛있는 메뉴")
			.price(15000)
			.status(MenuStatus.ON_SALE)
			.menuGroupId(1L)
			.optionGroupIds(List.of(10L, 11L))
			.build();

		MenuGroups menuGroup = TestFixture.createMenuGroup(1L, store);

		MockMultipartFile image = new MockMultipartFile(
			"image",
			"test.jpg",
			"image/jpeg",
			"dummy-image".getBytes()
		);

		S3UploadResponseDto s3Response = S3UploadResponseDto.builder()
			.imageUrl("https://cloudfront/test.jpg")
			.imageKey("menu/test.jpg")
			.build();

		Menus saved = Menus.builder()
			.id(1L)
			.name("새로운메뉴")
			.description("맛있는 메뉴")
			.price(15000)
			.menuStatus(MenuStatus.ON_SALE)
			.img(s3Response.imageUrl())
			.s3Key(s3Response.imageKey())
			.build();

		OptionGroups og1 = OptionGroups.builder()
			.id(10L)
			.name("옵션그룹1")
			.build();

		OptionGroups og2 = OptionGroups.builder()
			.id(11L)
			.name("옵션그룹2")
			.build();

		when(menuGroupRepository.findById(1L))
			.thenReturn(Optional.of(menuGroup));

		when(s3Service.uploadImage(any(MultipartFile.class)))
			.thenReturn(s3Response);

		when(menuRepository.save(any(Menus.class))).thenReturn(saved);

		when(optionGroupRepository.findAllById(List.of(10L, 11L)))
			.thenReturn(List.of(og1, og2));

		// when
		menuService.createMenu(request, image);

		// then
		verify(menuRepository, times(1)).save(any(Menus.class));
		verify(optionGroupRepository, times(1))
			.findAllById(List.of(10L, 11L));

		assertThat(saved.getName()).isEqualTo("새로운메뉴");
		assertThat(saved.getDescription()).isEqualTo("맛있는 메뉴");
		assertThat(saved.getMenuOptionGroups()).hasSize(2);
		assertThat(saved.getMenuOptionGroups())
			.extracting(m -> m.getOptionGroup().getId())
			.containsExactlyInAnyOrder(10L, 11L);
	}

	@Test
	@DisplayName("메뉴 상태 변경이 정상적으로 수행되는지 검증한다.")
	void changeMenuStatus() {
		// given
		Long menuId = 1L;
		MenuStatus newStatus = MenuStatus.SOLD_OUT;

		Menus menu = Menus.builder()
			.id(menuId)
			.name("양념꾸브")
			.description("양념")
			.price(18000)
			.menuStatus(MenuStatus.ON_SALE)
			.img("test.jpg")
			.build();

		when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));

		// when
		menuService.changeMenuStatus(menuId, newStatus);

		// then
		assertThat(menu.getMenuStatus()).isEqualTo(MenuStatus.SOLD_OUT);

		verify(menuRepository, times(1)).findById(menuId);
	}

}