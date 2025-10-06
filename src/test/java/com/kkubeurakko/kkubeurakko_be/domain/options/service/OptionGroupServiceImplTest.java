package com.kkubeurakko.kkubeurakko_be.domain.options.service;

import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionGroupRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroups;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.Status;
import com.kkubeurakko.kkubeurakko_be.domain.options.repository.OptionGroupRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class OptionGroupServiceImplTest {
    @Mock
    private OptionGroupRepository optionGroupRepository;

    @InjectMocks
    private OptionGroupServiceImpl optionGroupService;

    public OptionGroupServiceImplTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createOptionGroup_ShouldSaveOptionGroup() {
        // given
        OptionGroupRequestDto requestDto = OptionGroupRequestDto.builder()
                .name("맵기 선택")
                .description("맵기 단계를 선택해 주세요")
                .required(true)
                .optionGroupType(com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroupType.SINGLE)
                .build();

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
        assertEquals(com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroupType.SINGLE, saved.getOptionGroupType());
        assertEquals(Status.ON_SALE, saved.getStatus());
    }
}