package com.kkubeurakko.kkubeurakko_be.domain.options.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionGroupRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionGroupResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroups;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.Status;
import com.kkubeurakko.kkubeurakko_be.domain.options.repository.OptionGroupRepository;
import com.kkubeurakko.kkubeurakko_be.domain.options.repository.OptionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OptionGroupServiceImpl implements OptionGroupService {

	private final OptionGroupRepository optionGroupRepository;
	private final OptionRepository optionRepository;

	@Override
	public void createOptionGroup(OptionGroupRequestDto requestDto) {
		OptionGroups optionGroup = OptionGroups.builder()
			.name(requestDto.name())
			.description(requestDto.description())
			.required(requestDto.required())
			.optionGroupType(requestDto.optionGroupType())
			.status(Status.ON_SALE)
			.selectedNum(requestDto.selectedNum())
			.build();

		optionGroupRepository.save(optionGroup);
	}

	public OptionGroupResponseDto getOptionGroup(Long id) {
		OptionGroups optionGroup = optionGroupRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("해당 옵션 그룹이 없습니다. id=" + id));

		return toResponseDto(optionGroup);
	}

	public List<OptionGroupResponseDto> getAllOptionGroups() {
		return optionGroupRepository.findAll().stream()
			.map(this::toResponseDto)
			.toList();
	}

	@Override
	public void updateOptionGroup(Long id, OptionGroupRequestDto requestDto) {
		OptionGroups optionGroup = optionGroupRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("해당 옵션 그룹이 없습니다. id=" + id));

		optionGroup.update(
			requestDto.name(),
			requestDto.description(),
			requestDto.required(),
			requestDto.optionGroupType(),
			requestDto.selectedNum()
		);

		optionGroupRepository.save(optionGroup);
	}

	private OptionGroupResponseDto toResponseDto(OptionGroups optionGroup) {
		return OptionGroupResponseDto.builder()
			.id(optionGroup.getId())
			.name(optionGroup.getName())
			.description(optionGroup.getDescription())
			.required(optionGroup.isRequired())
			.status(optionGroup.getStatus())
			.optionGroupType(optionGroup.getOptionGroupType())
			.selectedNum(optionGroup.getSelectedNum())
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
