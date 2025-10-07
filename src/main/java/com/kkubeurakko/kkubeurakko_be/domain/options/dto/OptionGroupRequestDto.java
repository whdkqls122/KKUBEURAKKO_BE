package com.kkubeurakko.kkubeurakko_be.domain.options.dto;

import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroupType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
public record OptionGroupRequestDto(

        @NotBlank(message = "옵션 그룹 이름은 필수입니다.")
        @Size(max = 30, message = "옵션 그룹 이름은 최대 30자까지 가능합니다.")
        String name,

        @Size(max = 250, message = "설명은 최대 150자까지 가능합니다.")
        String description,

        @NotNull(message = "필수/추가 선택을 지정해야 합니다.")
        Boolean required,

        @NotNull(message = "단일/다중 선택을 지정해야 합니다.")
        OptionGroupType optionGroupType
) {}
