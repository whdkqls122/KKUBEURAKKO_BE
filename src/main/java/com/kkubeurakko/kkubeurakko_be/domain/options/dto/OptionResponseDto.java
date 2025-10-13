package com.kkubeurakko.kkubeurakko_be.domain.options.dto;

import com.kkubeurakko.kkubeurakko_be.domain.options.entity.Status;
import lombok.Builder;
import lombok.Getter;

@Builder
public record OptionResponseDto(
        Long id,
        String name,
        int price,
        Status status
) {}