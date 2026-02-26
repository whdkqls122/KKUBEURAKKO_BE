package com.kkubeurakko.kkubeurakko_be.domain.options.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroups;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionStatus;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.Options;
import com.kkubeurakko.kkubeurakko_be.domain.options.repository.OptionGroupRepository;
import com.kkubeurakko.kkubeurakko_be.domain.options.repository.OptionRepository;

class OptionServiceImplTest {

	@Mock
	private OptionRepository optionsRepository;

	@Mock
	private OptionGroupRepository optionGroupRepository;

	@InjectMocks
	private OptionServiceImpl optionService;

	public OptionServiceImplTest() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void createOption() {
		// given
		OptionGroups optionGroup = OptionGroups.builder()
			.id(1L)
			.name("맵기 선택")
			.description("맵기 단계를 선택해 주세요")
			.required(true)
			.optionStatus(OptionStatus.ON_SALE)
			.optionGroupType(com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroupType.SINGLE)
			.build();

		when(optionGroupRepository.findById(1L)).thenReturn(Optional.of(optionGroup));

		OptionRequestDto requestDto = new OptionRequestDto("순한맛", 2000, 1L);

		// when
		optionService.createOption(requestDto);

		// then
		ArgumentCaptor<Options> captor = ArgumentCaptor.forClass(Options.class);
		verify(optionsRepository, times(1)).save(captor.capture());

		Options saved = captor.getValue();

		System.out.println("저장된 옵션: " + saved);

		assertEquals("순한맛", saved.getName());
		assertEquals(2000, saved.getPrice());
		assertEquals(optionGroup, saved.getOptionGroup());
	}

	@Test
	void updateOption() {
		// given
		OptionGroups optionGroup = OptionGroups.builder()
			.id(1L)
			.name("맵기 선택")
			.description("맵기 단계를 선택해 주세요")
			.required(true)
			.optionStatus(OptionStatus.ON_SALE)
			.optionGroupType(com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroupType.SINGLE)
			.build();

		Options option = Options.builder()
			.id(1L)
			.name("순한맛")
			.price(2000)
			.optionStatus(OptionStatus.ON_SALE)
			.optionGroup(optionGroup)
			.build();

		when(optionsRepository.findById(1L)).thenReturn(Optional.of(option));

		//가격 변경
		OptionRequestDto requestDto = new OptionRequestDto("순한맛", 0, 1L);

		// when
		optionService.updateOption(1L, requestDto);

		// then
		ArgumentCaptor<Options> captor = ArgumentCaptor.forClass(Options.class);
		verify(optionsRepository).save(captor.capture());

		Options updated = captor.getValue();
		System.out.println("업데이트된 옵션: " + updated);

		assertEquals("순한맛", updated.getName());
		assertEquals(0, updated.getPrice());
	}

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

		Options option = Options.builder()
			.id(1L)
			.name("순한맛")
			.price(2000)
			.optionStatus(OptionStatus.ON_SALE)
			.optionGroup(optionGroup)
			.build();

		when(optionsRepository.findById(1L)).thenReturn(Optional.of(option));

		// when
		optionService.updateStatus(1L, OptionStatus.HIDE);

		// then
		ArgumentCaptor<Options> captor = ArgumentCaptor.forClass(Options.class);
		verify(optionsRepository).save(captor.capture());

		Options updated = captor.getValue();
		System.out.println("변경된 상태 옵션: " + updated);

		assertEquals(OptionStatus.HIDE, updated.getOptionStatus());
	}

	@Test
	void deleteOption() {
		OptionGroups optionGroup = OptionGroups.builder().id(1L).active(true).build();

		Options option = Options.builder()
			.id(1L)
			.name("순한맛")
			.price(2000)
			.optionStatus(OptionStatus.ON_SALE)
			.active(true)
			.optionGroup(optionGroup)
			.build();
		when(optionsRepository.findById(1L)).thenReturn(Optional.of(option));
		optionService.deleteOption(1L);

		assertFalse(option.isActive());
	}

}