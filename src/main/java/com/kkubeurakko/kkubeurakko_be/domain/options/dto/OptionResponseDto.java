package com.kkubeurakko.kkubeurakko_be.domain.options.dto;

import com.kkubeurakko.kkubeurakko_be.domain.options.entity.Status;


public record OptionResponseDto(
        Long id,
        String name,
        int price,
        Status status
) {}