package com.kkubeurakko.kkubeurakko_be.domain.options.service;

import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroups;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.Options;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.Status;
import com.kkubeurakko.kkubeurakko_be.domain.options.repository.OptionGroupRepository;
import com.kkubeurakko.kkubeurakko_be.domain.options.repository.OptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OptionServiceImpl implements OptionService {

    private final OptionRepository optionsRepository;
    private final OptionGroupRepository optionGroupsRepository;

    @Override
    public void createOption(OptionRequestDto requestDto) {
        OptionGroups optionGroup = optionGroupsRepository.findById(requestDto.optionGroupId())
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 옵션 그룹이 존재하지 않습니다."));

        Options option = Options.builder()
                .name(requestDto.name())
                .price(requestDto.price())
                .status(Status.ON_SALE)
                .optionGroup(optionGroup)
                .build();
        optionsRepository.save(option);
    }
}
