package com.kkubeurakko.kkubeurakko_be.domain.options.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionGroupNameResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionGroupRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionGroupResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.dto.OptionResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroups;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionStatus;
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
			.optionStatus(OptionStatus.ON_SALE)
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

	public List<OptionGroupNameResponseDto> getAllOptionGroupName() {
		return optionGroupRepository.findAll().stream()
			.map(this::toGroupNameResponseDto)
			.collect(Collectors.toList());
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

	@Override
	public void deleteOptionGroup(Long id) {
		OptionGroups optionGroup = optionGroupRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("해당 옵션 그룹이 없습니다. id=" + id));

		optionGroupRepository.delete(optionGroup);
	}

	@Override
	@Transactional
	public void updateStatus(Long id, OptionStatus newOptionStatus) {
		OptionGroups optionGroup = optionGroupRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("해당 옵션 그룹이 없습니다. id=" + id));

		optionGroup.updateStatus(newOptionStatus);
	}

	private OptionGroupResponseDto toResponseDto(OptionGroups optionGroup) {
		return OptionGroupResponseDto.builder()
			.id(optionGroup.getId())
			.name(optionGroup.getName())
			.description(optionGroup.getDescription())
			.required(optionGroup.isRequired())
			.optionStatus(optionGroup.getOptionStatus())
			.optionGroupType(optionGroup.getOptionGroupType())
			.selectedNum(optionGroup.getSelectedNum())
			.options(
				optionGroup.getOptions().stream()
					.map(option -> OptionResponseDto.builder()
						.id(option.getId())
						.name(option.getName())
						.price(option.getPrice())
						.optionStatus(option.getOptionStatus())
						.build())
					.toList()
			)
			.build();
	}

	private OptionGroupNameResponseDto toGroupNameResponseDto(OptionGroups optionGroup) {
		return OptionGroupNameResponseDto.builder()
			.id(optionGroup.getId())
			.name(optionGroup.getName())
			.build();
	}
}
