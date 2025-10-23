package com.kkubeurakko.kkubeurakko_be.domain.options.service;

import org.springframework.stereotype.Service;

import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroups;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.Options;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.Status;
import com.kkubeurakko.kkubeurakko_be.domain.options.repository.OptionGroupRepository;
import com.kkubeurakko.kkubeurakko_be.domain.options.repository.OptionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OptionServiceImpl implements OptionService {

	private final OptionRepository optionRepository;
	private final OptionGroupRepository optionGroupRepository;

	@Override
	public void createOption(OptionRequestDto requestDto) {
		OptionGroups optionGroup = optionGroupRepository.findById(requestDto.optionGroupId())
			.orElseThrow(() -> new IllegalArgumentException("해당 ID의 옵션 그룹이 존재하지 않습니다."));

		Options option = Options.builder()
			.name(requestDto.name())
			.price(requestDto.price())
			.status(Status.ON_SALE)
			.optionGroup(optionGroup)
			.build();
		optionRepository.save(option);
	}

	@Override
	public void updateOption(Long optionId, OptionRequestDto requestDto) {
		Options option = optionRepository.findById(optionId)
			.orElseThrow(() -> new IllegalArgumentException("해당 ID의 옵션이 존재하지 않습니다."));

		OptionGroups optionGroup = null;
		if (requestDto.optionGroupId() != null) {
			optionGroup = optionGroupRepository.findById(requestDto.optionGroupId())
				.orElseThrow(() -> new IllegalArgumentException("해당 ID의 옵션 그룹이 존재하지 않습니다."));
		}

		option.update(requestDto.name(), requestDto.price(), optionGroup);
		optionRepository.save(option);
	}

	public void updateStatus(Long optionId, Status newStatus) {
		Options option = optionRepository.findById(optionId)
			.orElseThrow(() -> new IllegalArgumentException("해당 ID의 옵션이 존재하지 않습니다."));

		option.updateStatus(newStatus);
		optionRepository.save(option);
	}

	@Override
	public void deleteOption(Long optionId) {
		Options option = optionRepository.findById(optionId)
			.orElseThrow(() -> new IllegalArgumentException("해당 ID의 옵션이 존재하지 않습니다."));
		optionRepository.delete(option);
	}
}
