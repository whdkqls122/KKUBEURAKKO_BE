package com.kkubeurakko.kkubeurakko_be.domain.options.service;

import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionGroupRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroups;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.Status;
import com.kkubeurakko.kkubeurakko_be.domain.options.repository.OptionGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OptionGroupServiceImpl implements OptionGroupService {

    private final OptionGroupRepository optionGroupsRepository;

    @Override
    public void createOptionGroup(OptionGroupRequestDto requestDto) {
        OptionGroups optionGroup = OptionGroups.builder()
                .name(requestDto.getName())
                .description(requestDto.getDescription())
                .required(requestDto.getRequired())
                .optionGroupType(requestDto.getOptionGroupType())
                .status(Status.ON_SALE)
                .build();
        optionGroupsRepository.save(optionGroup);
    }


}
