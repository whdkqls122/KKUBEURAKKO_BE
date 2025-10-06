package com.kkubeurakko.kkubeurakko_be.domain.options.dto;

import com.kkubeurakko.kkubeurakko_be.domain.options.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptionResponseDto {
    private Long id;
    private String name;
    private int price;
    private Status status;
}