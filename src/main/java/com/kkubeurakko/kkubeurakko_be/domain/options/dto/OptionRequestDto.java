package com.kkubeurakko.kkubeurakko_be.domain.options.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
public record OptionRequestDto(

        @NotBlank(message = "옵션 이름은 필수입니다.")
        @Size(max = 30, message = "옵션 이름은 최대 30자까지 가능합니다.")
        String name,

        @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
        int price,

        @NotNull(message = "옵션 그룹 ID는 필수입니다.")
        Long optionGroupId
) {}