package com.kkubeurakko.kkubeurakko_be.domain.addresses.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kkubeurakko.kkubeurakko_be.domain.addresses.dto.AddressRequestDto;
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
		CustomOAuth2User customOAuth2User) {
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
		AddressRequestDto addressRequestDto,
		@AuthenticationPrincipal
		CustomOAuth2User customOAuth2User) {
		addressService.updateAddress(addressId, addressRequestDto, customOAuth2User);

		return ResponseEntity.ok().body(
			ApiResponse.success(
				"주소 수정이 완료되었습니다.",
				null

			)
		);
	}
}
