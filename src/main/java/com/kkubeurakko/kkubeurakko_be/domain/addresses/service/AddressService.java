package com.kkubeurakko.kkubeurakko_be.domain.addresses.service;

import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.addresses.dto.AddressDetailResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.dto.AddressRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.dto.AddressResponseDto;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

public interface AddressService {
	void createAddress(AddressRequestDto addressRequestDto, CustomOAuth2User customOAuth2User);

	void updateAddress(Long addressId, AddressRequestDto addressRequestDto);

	void updatePrimary(Long addressId, CustomOAuth2User customOAuth2User);

	List<AddressResponseDto> getAllAddresses(CustomOAuth2User customOAuth2User);

	AddressResponseDto getAddress(CustomOAuth2User customOAuth2User);

	AddressDetailResponseDto getAddressDetail(Long addressId, CustomOAuth2User customOAuth2User);

	void deleteAddress(Long addressId, CustomOAuth2User customOAuth2User);

	void deleteOldAddress(CustomOAuth2User customOAuth2User);
}
