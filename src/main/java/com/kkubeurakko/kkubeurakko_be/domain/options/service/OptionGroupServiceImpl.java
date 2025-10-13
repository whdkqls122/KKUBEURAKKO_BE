package com.kkubeurakko.kkubeurakko_be.domain.options.service;

import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionGroupRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionGroupResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroups;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.Status;
import com.kkubeurakko.kkubeurakko_be.domain.options.repository.OptionGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OptionGroupServiceImpl implements OptionGroupService {

    private final OptionGroupRepository optionGroupsRepository;

    @Override
    public void createOptionGroup(OptionGroupRequestDto requestDto) {
        OptionGroups optionGroup = OptionGroups.builder()
                .name(requestDto.name())
                .description(requestDto.description())
                .required(requestDto.required())
                .optionGroupType(requestDto.optionGroupType())
                .status(Status.ON_SALE)
                .build();
        optionGroupsRepository.save(optionGroup);
    }

    public OptionGroupResponseDto getOptionGroup(Long id) {
        OptionGroups optionGroup = optionGroupsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 옵션 그룹이 없습니다. id=" + id));

        return toResponseDto(optionGroup);
    }

    public List<OptionGroupResponseDto> getAllOptionGroups() {
        return optionGroupsRepository.findAll().stream()
                .map(this::toResponseDto)
                .toList();
    }

    private OptionGroupResponseDto toResponseDto(OptionGroups optionGroup) {
        return OptionGroupResponseDto.builder()
                .id(optionGroup.getId())
                .name(optionGroup.getName())
                .description(optionGroup.getDescription())
                .required(optionGroup.isRequired())
                .status(optionGroup.getStatus())
                .optionGroupType(optionGroup.getOptionGroupType())
                .options(
                        optionGroup.getOptions().stream()
                                .map(option -> OptionResponseDto.builder()
                                        .id(option.getId())
                                        .name(option.getName())
                                        .price(option.getPrice())
                                        .status(option.getStatus())
                                        .build())
                                .toList()
                )
                .build();
    }

}
