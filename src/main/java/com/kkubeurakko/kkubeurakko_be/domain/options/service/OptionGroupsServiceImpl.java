package com.kkubeurakko.kkubeurakko_be.domain.options.service;

import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionGroupRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroups;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.Status;
import com.kkubeurakko.kkubeurakko_be.domain.options.repository.OptionGroupsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OptionGroupsServiceImpl implements OptionGroupsService {

    private final OptionGroupsRepository optionGroupsRepository;

    @Override
    public void createOptionGroup(OptionGroupRequestDto requestDto) {
        OptionGroups optionGroup = OptionGroups.builder()
                .name(requestDto.getName())
                .description(requestDto.getDescription())
                .required(requestDto.isRequired())
                .optionGroupType(requestDto.getOptionGroupType())
                .status(Status.ON_SALE)
                .build();
        optionGroupsRepository.save(optionGroup);
    }


}
