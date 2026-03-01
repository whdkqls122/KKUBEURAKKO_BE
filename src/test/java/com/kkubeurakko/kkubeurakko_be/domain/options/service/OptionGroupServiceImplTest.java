package com.kkubeurakko.kkubeurakko_be.domain.options.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionGroupNameResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionGroupRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionGroupResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroupType;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroups;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionStatus;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.Options;
import com.kkubeurakko.kkubeurakko_be.domain.options.repository.OptionGroupRepository;

class OptionGroupServiceImplTest {
	@Mock
	private OptionGroupRepository optionGroupRepository;

	@InjectMocks
	private OptionGroupServiceImpl optionGroupService;

	public OptionGroupServiceImplTest() {
		MockitoAnnotations.openMocks(this);
	}

	//옵션 그룹 생성
	@Test
	void createOptionGroup() {
		// given (record는 new로 생성)
		OptionGroupRequestDto requestDto = new OptionGroupRequestDto(
			"맵기 선택",
			"맵기 단계를 선택해 주세요",
			true,
			com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroupType.SINGLE,
			3
		);

		// when
		optionGroupService.createOptionGroup(requestDto);

		// then
		ArgumentCaptor<OptionGroups> captor = ArgumentCaptor.forClass(OptionGroups.class);
		verify(optionGroupRepository, times(1)).save(captor.capture());

		OptionGroups saved = captor.getValue();

		System.out.println("저장된 옵션 그룹: " + saved);

		assertEquals("맵기 선택", saved.getName());
		assertEquals("맵기 단계를 선택해 주세요", saved.getDescription());
		assertTrue(saved.isRequired());
		assertEquals(com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroupType.SINGLE,
			saved.getOptionGroupType());
		assertEquals(OptionStatus.ON_SALE, saved.getOptionStatus());
	}

	//옵션 그룹 단건 조회
	@Test
	void getOptionGroup() {
		// given
		OptionGroups group = OptionGroups.builder()
			.id(1L)
			.name("맵기 선택")
			.description("맵기 단계를 선택")
			.required(true)
			.optionGroupType(OptionGroupType.SINGLE)
			.optionStatus(OptionStatus.ON_SALE)
			.build();

		Options option1 = Options.builder()
			.id(1L)
			.name("순한맛")
			.price(0)
			.optionStatus(OptionStatus.ON_SALE)
			.optionGroup(group)
			.build();
		Options option2 = Options.builder()
			.id(2L)
			.name("보통")
			.price(500)
			.optionStatus(OptionStatus.ON_SALE)
			.optionGroup(group)
			.build();

		group.getOptions().add(option1);
		group.getOptions().add(option2);

		when(optionGroupRepository.findById(1L)).thenReturn(Optional.of(group));

		// when
		OptionGroupResponseDto responseDto = optionGroupService.getOptionGroup(1L);

		// then
		assertEquals(group.getId(), responseDto.id());
		assertEquals(2, responseDto.options().size());
		assertEquals("순한맛", responseDto.options().get(0).name());
		assertEquals("보통", responseDto.options().get(1).name());
		verify(optionGroupRepository, times(1)).findById(1L);

		//출력
		System.out.println("=== 단건 조회 결과 ===");
		System.out.println("그룹 ID: " + responseDto.id());
		System.out.println("그룹 이름: " + responseDto.name());
		System.out.println("그룹 설명: " + responseDto.description());
		System.out.println("필수 여부: " + responseDto.required());
		System.out.println("옵션 그룹 타입: " + responseDto.optionGroupType());
		System.out.println("옵션 개수: " + responseDto.options().size());
		System.out.println("옵션 목록:");
		responseDto.options().forEach(opt ->
			System.out.println(" - " + opt.name() + " (" + opt.price() + "원, 상태: " + opt.optionStatus() + ")")
		);
	}

	// 옵션 그룹 전체 조회
	@Test
	void getAllOptionGroups() {
		// given
		OptionGroups group1 = OptionGroups.builder()
			.id(1L)
			.name("맵기 선택")
			.description("맵기 단계를 선택")
			.required(true)
			.optionGroupType(OptionGroupType.SINGLE)
			.optionStatus(OptionStatus.ON_SALE)
			.options(new ArrayList<>())
			.build();

		Options option1 = Options.builder()
			.id(1L)
			.name("순한맛")
			.price(0)
			.optionStatus(OptionStatus.ON_SALE)
			.optionGroup(group1)
			.build();

		Options option2 = Options.builder()
			.id(2L)
			.name("보통맛")
			.price(500)
			.optionStatus(OptionStatus.ON_SALE)
			.optionGroup(group1)
			.build();

		group1.getOptions().add(option1);
		group1.getOptions().add(option2);

		OptionGroups group2 = OptionGroups.builder()
			.id(2L)
			.name("부위")
			.description("부위를 선택하세요")
			.required(true)
			.optionGroupType(OptionGroupType.SINGLE)
			.optionStatus(OptionStatus.ON_SALE)
			.options(new ArrayList<>())
			.build();

		Options option3 = Options.builder()
			.id(3L)
			.name("뼈")
			.price(0)
			.optionStatus(OptionStatus.ON_SALE)
			.optionGroup(group2)
			.build();

		Options option4 = Options.builder()
			.id(4L)
			.name("콤보")
			.price(2000)
			.optionStatus(OptionStatus.ON_SALE)
			.optionGroup(group2)
			.build();

		group2.getOptions().add(option3);
		group2.getOptions().add(option4);

		when(optionGroupRepository.findAll()).thenReturn(List.of(group1, group2));

		// when
		List<OptionGroupResponseDto> responseList = optionGroupService.getAllOptionGroups();

		// then
		assertEquals(2, responseList.size());
		assertEquals("맵기 선택", responseList.get(0).name());
		assertEquals("부위", responseList.get(1).name());
		assertEquals(2, responseList.get(0).options().size());
		assertEquals(2, responseList.get(1).options().size());
		verify(optionGroupRepository, times(1)).findAll();

		// 출력
		System.out.println("=== 전체 옵션 그룹 조회 결과 ===");
		responseList.forEach(group -> {
			System.out.println("그룹 ID: " + group.id());
			System.out.println("그룹 이름: " + group.name());
			System.out.println("설명: " + group.description());
			System.out.println("필수 여부: " + group.required());
			System.out.println("옵션 그룹 타입: " + group.optionGroupType());
			System.out.println("옵션 목록:");
			group.options().forEach(opt ->
				System.out.println(" - " + opt.name() + " (" + opt.price() + "원, 상태: " + opt.optionStatus() + ")")
			);
			System.out.println("------------------------------");
		});
	}

	@Test
	@DisplayName("옵션 그룹의 이름과 id 리스트가 정상적으로 조회되는지 검증한다.")
	void getAllOptionGroupName() {
		// given
		OptionGroups group1 = OptionGroups.builder()
			.id(1L)
			.name("맵기 선택")
			.build();

		OptionGroups group2 = OptionGroups.builder()
			.id(2L)
			.name("부위")
			.description("부위를 선택하세요")
			.build();

		when(optionGroupRepository.findAll()).thenReturn(List.of(group1, group2));

		// when
		List<OptionGroupNameResponseDto> responseList = optionGroupService.getAllOptionGroupName();

		// then
		assertEquals(2, responseList.size());
		assertEquals("맵기 선택", responseList.get(0).name());
		assertEquals("부위", responseList.get(1).name());
		verify(optionGroupRepository, times(1)).findAll();

		// 출력
		System.out.println("=== 전체 옵션 그룹 조회 결과 ===");
		responseList.forEach(group -> {
			System.out.println("그룹 ID: " + group.id());
			System.out.println("그룹 이름: " + group.name());
			System.out.println("------------------------------");
		});
	}

	// 옵션 그룹 수정
	@Test
	void updateOptionGroup() {
		// given
		OptionGroups group = OptionGroups.builder()
			.id(1L)
			.name("맵기 선택")
			.description("맵기 단계를 선택")
			.required(true)
			.optionGroupType(OptionGroupType.SINGLE)
			.optionStatus(OptionStatus.ON_SALE)
			.build();

		when(optionGroupRepository.findById(1L)).thenReturn(Optional.of(group));

		OptionGroupRequestDto updateDto = new OptionGroupRequestDto(
			"매운맛 선택",
			"매운 단계를 선택하세요",
			false,
			OptionGroupType.MULTI,
			5
		);

		// when
		optionGroupService.updateOptionGroup(1L, updateDto);

		// then
		ArgumentCaptor<OptionGroups> captor = ArgumentCaptor.forClass(OptionGroups.class);
		verify(optionGroupRepository).save(captor.capture());

		OptionGroups updated = captor.getValue();
		System.out.println("업데이트된 옵션 그룹: " + updated);

		assertEquals("매운맛 선택", updated.getName());
		assertEquals("매운 단계를 선택하세요", updated.getDescription());
		assertFalse(updated.isRequired());
		assertEquals(OptionGroupType.MULTI, updated.getOptionGroupType());
		assertEquals(5, updated.getSelectedNum());
	}

	// 옵션 그룹 삭제
	@Test
	void deleteOptionGroup_ShouldDeleteOptionGroup() {
		OptionGroups group = OptionGroups.builder()
			.id(1L)
			.name("맵기 선택")
			.description("맵기 단계를 선택")
			.required(true)
			.optionGroupType(OptionGroupType.SINGLE)
			.optionStatus(OptionStatus.ON_SALE)
			.options(new ArrayList<>())
			.build();

		Options option1 = Options.builder()
			.id(1L).name("순한맛").price(0).optionStatus(OptionStatus.ON_SALE)
			.optionGroup(group).build();
		group.getOptions().add(option1);

		when(optionGroupRepository.findById(1L)).thenReturn(Optional.of(group));

		optionGroupService.deleteOptionGroup(1L);

		verify(optionGroupRepository, never()).delete(any());

		assertEquals(group.getOptionStatus(), OptionStatus.DELETED);
		assertEquals(option1.getOptionStatus(), OptionStatus.DELETED);
	}

	//옵션 그룹 상태 변경
	@Test
	void updateStatus() {
		// given
		OptionGroups optionGroup = OptionGroups.builder()
			.id(1L)
			.name("맵기 선택")
			.description("맵기 단계를 선택해 주세요")
			.required(true)
			.optionStatus(OptionStatus.ON_SALE)
			.optionGroupType(com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroupType.SINGLE)
			.build();

		when(optionGroupRepository.findById(1L))
			.thenReturn(Optional.of(optionGroup));

		// when
		optionGroupService.updateStatus(1L, OptionStatus.HIDE);

		// then
		assertEquals(OptionStatus.HIDE, optionGroup.getOptionStatus());
		verify(optionGroupRepository).findById(1L);
		verify(optionGroupRepository, never()).save(any());
	}
}