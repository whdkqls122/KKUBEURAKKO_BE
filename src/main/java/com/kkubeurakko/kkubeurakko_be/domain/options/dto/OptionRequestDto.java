package com.kkubeurakko.kkubeurakko_be.domain.options.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptionRequestDto {
    private String name;
    private int price;
    private Long optionGroupId;
}
