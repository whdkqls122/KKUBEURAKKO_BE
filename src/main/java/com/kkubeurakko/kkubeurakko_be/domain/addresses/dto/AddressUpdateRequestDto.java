package com.kkubeurakko.kkubeurakko_be.domain.addresses.dto;

import com.kkubeurakko.kkubeurakko_be.domain.addresses.entity.AddressCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record AddressUpdateRequestDto(
	@Size(max = 50, message = "주소 이름은 최대 50자까지 가능합니다.")
	String addressName,
	@NotBlank(message = "도로명 주소는 필수입니다.")
	@Size(max = 50, message = "도로명 주소는 최대 50자까지 가능합니다.")
	String roadName,
	@NotBlank(message = "상세 주소는 필수입니다.")
	@Size(max = 20, message = "상세 주소는 최대 20자까지 가능합니다.")
	String detailAddress,
	@NotBlank(message = "상세 주소는 필수 입니다.")
	@Pattern(regexp = "\\d{5}", message = "우편번호는 숫자 5자리로 구성되어 있어야 합니다.")
	String postalCode,
	@NotNull(message = "주소 카테고리는 필수입니다.")
	AddressCategory category,
	@Size(max = 100, message = "배달 요청사항은 최대 100자까지 가능합니다.")
	String requestContent,
	@Size(max = 20, message = "공동 현관 비밀번호는 최대 20자까지 가능합니다.")
	String entrancePassword,
	@Size(max = 100, message = "찾아오는 길 정보는 최대 100자까지 가능합니다.")
	String directionInfo
) {
}