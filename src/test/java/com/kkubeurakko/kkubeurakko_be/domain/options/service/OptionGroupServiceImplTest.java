package com.kkubeurakko.kkubeurakko_be.domain.options.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionGroupRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionGroupResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroupType;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroups;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.Options;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.Status;
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
		assertEquals(Status.ON_SALE, saved.getStatus());
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
			.status(Status.ON_SALE)
			.build();

		Options option1 = Options.builder()
			.id(1L)
			.name("순한맛")
			.price(0)
			.status(Status.ON_SALE)
			.optionGroup(group)
			.build();
		Options option2 = Options.builder()
			.id(2L)
			.name("보통")
			.price(0)
			.status(Status.ON_SALE)
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

		System.out.println("=== 단건 조회 결과 ===");
		System.out.println("그룹 ID: " + responseDto.id());
		System.out.println("그룹 이름: " + responseDto.name());
		System.out.println("그룹 설명: " + responseDto.description());
		System.out.println("필수 여부: " + responseDto.required());
		System.out.println("옵션 그룹 타입: " + responseDto.optionGroupType());
		System.out.println("옵션 개수: " + responseDto.options().size());
		System.out.println("옵션 목록:");
		responseDto.options().forEach(opt ->
			System.out.println(" - " + opt.name() + " (" + opt.price() + "원, 상태: " + opt.status() + ")")
		);
	}
}