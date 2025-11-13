package com.kkubeurakko.kkubeurakko_be.domain.addresses.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kkubeurakko.kkubeurakko_be.domain.addresses.dto.AddressDetailResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.dto.AddressRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.dto.AddressResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.service.AddressService;
import com.kkubeurakko.kkubeurakko_be.global.ApiResponse;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Address API", description = "주소 관련 API")
@RestController
@RequestMapping("/v1/address")
@RequiredArgsConstructor
public class AddressController {
	private final AddressService addressService;

	@Operation(
		summary = "주소 저장",
		description = "주소를 저장하는 API입니다."
	)
	@PostMapping("")
	public ResponseEntity<ApiResponse<Void>> createAddress(
		@Valid @RequestBody
		AddressRequestDto addressRequestDto,
		@AuthenticationPrincipal
		CustomOAuth2User customOAuth2User
	) {
		addressService.createAddress(addressRequestDto, customOAuth2User);

		return ResponseEntity.ok().body(
			ApiResponse.success(
				"주소 등록이 완료되었습니다.",
				null

			)
		);
	}

	@Operation(
		summary = "주소 수정",
		description = "주소를 수정하는 API입니다."
	)
	@PutMapping("/{addressId}")
	public ResponseEntity<ApiResponse<Void>> updateAddress(
		@PathVariable("addressId")
		Long addressId,
		@Valid @RequestBody
		AddressRequestDto addressRequestDto
	) {
		addressService.updateAddress(addressId, addressRequestDto);

		return ResponseEntity.ok().body(
			ApiResponse.success(
				"주소 수정이 완료되었습니다.",
				null

			)
		);
	}

	@Operation(
		summary = "기본 주소 여부 수정",
		description = "사용 주소 변경시 기본 주소를 변경하는 API입니다."
	)
	@PutMapping("/{addressId}/primary")
	public ResponseEntity<ApiResponse<Void>> updatePrimary(
		@PathVariable("addressId")
		Long addressId,
		@AuthenticationPrincipal
		CustomOAuth2User customOAuth2User
	) {
		addressService.updatePrimary(addressId, customOAuth2User);

		return ResponseEntity.ok().body(
			ApiResponse.success(
				"기본 주소 수정이 완료되었습니다.",
				null
			)
		);
	}

	@Operation(
		summary = "주소 전체 조회",
		description = "주소 목록을 조회하는 API입니다."
	)
	@GetMapping("/addresslist")
	public ResponseEntity<ApiResponse<List<AddressResponseDto>>> getAllAddresses(
		@AuthenticationPrincipal
		CustomOAuth2User customOAuth2User
	) {
		List<AddressResponseDto> addressResponseDtoList = addressService.getAllAddresses(customOAuth2User);

		return ResponseEntity.ok().body(
			ApiResponse.success(
				"주소 목록 조회가 완료되었습니다.",
				addressResponseDtoList
			)
		);
	}

	@Operation(
		summary = "기본 주소 조회",
		description = "기본 주소를 조회하는 API입니다."
	)
	@GetMapping("/primary")
	public ResponseEntity<ApiResponse<AddressResponseDto>> getAddress(
		@AuthenticationPrincipal
		CustomOAuth2User customOAuth2User
	) {
		AddressResponseDto addressResponseDto = addressService.getAddress(customOAuth2User);

		return ResponseEntity.ok().body(
			ApiResponse.success(
				"기본 주소 조회가 완료되었습니다.",
				addressResponseDto
			)
		);
	}

	@Operation(
		summary = "주소 상세 조회",
		description = "특정 주소를 상세 조회하는 API입니다."
	)
	@GetMapping("/detail/{addressId}")
	public ResponseEntity<ApiResponse<AddressDetailResponseDto>> getAddressDetail(
		@PathVariable("addressId")
		Long addressId,
		@AuthenticationPrincipal
		CustomOAuth2User customOAuth2User
	) {
		AddressDetailResponseDto addressDetailResponseDto =
			addressService.getAddressDetail(addressId, customOAuth2User);

		return ResponseEntity.ok().body(
			ApiResponse.success(
				"주소 상세 조회가 완료되었습니다.",
				addressDetailResponseDto
			)
		);
	}

	@Operation(
		summary = "주소 삭제",
		description = "특정 주소를 삭제하는 API입니다."
	)
	@DeleteMapping("/{addressId}")
	public ResponseEntity<ApiResponse<Void>> deleteAddress(
		@PathVariable("addressId")
		Long addressId,
		@AuthenticationPrincipal
		CustomOAuth2User customOAuth2User
	) {
		addressService.deleteAddress(addressId, customOAuth2User);
		return ResponseEntity.ok().body(
			ApiResponse.success(
				"주소 삭제가 완료되었습니다.",
				null
			)
		);
	}

	@Operation(
		summary = "주소 삭제",
		description = "특정 주소를 삭제하는 API입니다."
	)
	@DeleteMapping("")
	public ResponseEntity<ApiResponse<Void>> deleteOldAddress(
		@AuthenticationPrincipal
		CustomOAuth2User customOAuth2User
	) {
		addressService.deleteOldAddress(customOAuth2User);
		return ResponseEntity.ok().body(
			ApiResponse.success(
				"주소 삭제가 완료되었습니다.",
				null
			)
		);
	}
}
