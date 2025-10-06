package com.kkubeurakko.kkubeurakko_be.domain.options.service;

import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroups;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.Options;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.Status;
import com.kkubeurakko.kkubeurakko_be.domain.options.repository.OptionGroupsRepository;
import com.kkubeurakko.kkubeurakko_be.domain.options.repository.OptionsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OptionsServiceImpl implements OptionsService {

    private final OptionsRepository optionsRepository;
    private final OptionGroupsRepository optionGroupsRepository;

    @Override
    public void createOption(OptionRequestDto requestDto) {
        OptionGroups optionGroup = optionGroupsRepository.findById(requestDto.getOptionGroupId())
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 옵션 그룹이 존재하지 않습니다."));

        Options option = Options.builder()
                .name(requestDto.getName())
                .price(requestDto.getPrice())
                .optionGroup(optionGroup)
                .build();
        optionsRepository.save(option);
    }
}
