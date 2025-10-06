package com.kkubeurakko.kkubeurakko_be.domain.options.dto;

import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroupType;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public record OptionGroupResponseDto(
        Long id,
        String name,
        String description,
        boolean required,
        OptionGroupType optionGroupType,
        Status status
) {}