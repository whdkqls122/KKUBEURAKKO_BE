package com.kkubeurakko.kkubeurakko_be.domain.options.service;

import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroups;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.Options;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.Status;
import com.kkubeurakko.kkubeurakko_be.domain.options.repository.OptionGroupRepository;
import com.kkubeurakko.kkubeurakko_be.domain.options.repository.OptionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OptionServiceImplTest {

    @Mock
    private OptionRepository optionsRepository;

    @Mock
    private OptionGroupRepository optionGroupsRepository;

    @InjectMocks
    private OptionServiceImpl optionService;

    public OptionServiceImplTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createOption_ShouldSaveOption() {
        // given
        OptionGroups optionGroup = OptionGroups.builder()
                .id(1L)
                .name("맵기 선택")
                .description("맵기 단계를 선택해 주세요")
                .required(true)
                .status(Status.ON_SALE)
                .optionGroupType(com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroupType.SINGLE)
                .build();

        when(optionGroupsRepository.findById(1L)).thenReturn(Optional.of(optionGroup));

        OptionRequestDto requestDto = OptionRequestDto.builder()
                .name("순한맛")
                .price(2000)
                .optionGroupId(1L)
                .build();

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
}